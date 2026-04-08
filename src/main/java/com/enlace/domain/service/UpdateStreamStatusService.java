package com.enlace.domain.service;

import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.UpdateStreamStatusUseCase;
import com.enlace.domain.port.out.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UpdateStreamStatusService implements UpdateStreamStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateStreamStatusService.class);

    private final EventRepository eventRepository;
    private final com.enlace.domain.port.out.IvsGateway ivsGateway;

    public UpdateStreamStatusService(EventRepository eventRepository, com.enlace.domain.port.out.IvsGateway ivsGateway) {
        this.eventRepository = eventRepository;
        this.ivsGateway = ivsGateway;
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
                    recording -> log.info("Gravacao processada: masterKey={} duration={}ms", recording.masterPlaylistKey(), recording.durationMs()),
                    () -> log.warn("recording-ended.json nao encontrado para prefixo: {}", recordingS3KeyPrefix)
                );
            }
            default -> log.warn("eventName '{}' nao reconhecido — ignorando", eventName);
        }
    }
}
