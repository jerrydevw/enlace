package com.enlace.domain.service;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventRecording;
import com.enlace.domain.port.in.UpdateStreamStatusUseCase;
import com.enlace.domain.port.out.EventRecordingRepository;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.IvsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateStreamStatusService implements UpdateStreamStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateStreamStatusService.class);

    private final EventRepository eventRepository;
    private final IvsGateway ivsGateway;
    private final EventRecordingRepository eventRecordingRepository;

    public UpdateStreamStatusService(EventRepository eventRepository,
                                     IvsGateway ivsGateway,
                                     EventRecordingRepository eventRecordingRepository) {
        this.eventRepository = eventRepository;
        this.ivsGateway = ivsGateway;
        this.eventRecordingRepository = eventRecordingRepository;
    }

    @Override
    @Transactional
    public void update(String channelName, String eventName, String streamId, String recordingS3KeyPrefix) {
        // channelName = slug do evento (usado como nome do canal IVS)
        Optional<Event> eventOpt = eventRepository.findBySlug(channelName);

        if (eventOpt.isEmpty()) {
            log.warn("Evento nao encontrado para channel '{}' — ignorando", channelName);
            return;
        }

        Event event = eventOpt.get();

        switch (eventName) {
            case "Stream Start" -> {
                log.info("Marcando evento '{}' como LIVE", channelName);
                event.markLive();
                eventRepository.save(event);
            }
            case "Stream End" -> {
                log.info("Marcando evento '{}' como ENDED — streamId: {}", channelName, streamId);
                event.markEnded();
                eventRepository.save(event);
                // A gravacao so estara disponivel no S3 apos o IVS processar o arquivo.
                // O evento "Recording End" do EventBridge dispara exatamente quando isso ocorre.
            }
            case "Recording End" -> {
                if (recordingS3KeyPrefix == null) {
                    log.warn("Recording End recebido sem recordingS3KeyPrefix para channel '{}' — ignorando", channelName);
                    return;
                }
                log.info("Gravacao disponivel para o evento '{}' (prefixo: {})", channelName, recordingS3KeyPrefix);

                ivsGateway.findRecording(recordingS3KeyPrefix).ifPresentOrElse(
                    recording -> {
                        Instant now = Instant.now();

                        // master playlist
                        eventRecordingRepository.save(new EventRecording(
                            UUID.randomUUID(), event.getId(),
                            recording.masterPlaylistKey(), "master",
                            recording.durationMs(), now
                        ));

                        // uma linha por resolucao (160p30, 360p30, 480p30, etc.)
                        recording.renditions().forEach(rendition ->
                            eventRecordingRepository.save(new EventRecording(
                                UUID.randomUUID(), event.getId(),
                                rendition.s3Key(), rendition.quality(),
                                recording.durationMs(), now
                            ))
                        );

                        log.info("Gravacoes salvas: eventId={} resolucoes={} duration={}ms",
                            event.getId(),
                            recording.renditions().stream().map(IvsGateway.RenditionInfo::quality).toList(),
                            recording.durationMs());
                    },
                    () -> log.warn("recording-ended.json nao encontrado para prefixo: {}", recordingS3KeyPrefix)
                );

                event.markRecordingAvailable();
                eventRepository.save(event);
                log.info("recording_available marcado como true para o evento '{}'", channelName);
            }
            default -> log.warn("eventName '{}' nao reconhecido — ignorando", eventName);
        }
    }
}
