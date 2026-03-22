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

    public UpdateStreamStatusService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public void update(String channelName, String eventName) {
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
                log.info("Marcando evento '{}' como ENDED", channelName);
                event.markEnded();
                eventRepository.save(event);
            }
            default -> log.warn("eventName '{}' nao reconhecido — ignorando", eventName);
        }
    }
}
