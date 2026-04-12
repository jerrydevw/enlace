package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.IvsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StopStreamService {

    private final EventRepository eventRepository;
    private final IvsGateway ivsGateway;
    private final AuditService auditService;

    @Transactional
    public void stop(UUID eventId, UUID customerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        if (event.getStatus() == EventStatus.ENDED) {
            log.info("Evento {} já está ENDED — nenhuma ação necessária", eventId);
            return;
        }

        if (event.getIvsChannelArn() != null &&
                (event.getStatus() == EventStatus.LIVE || event.getStatus() == EventStatus.READY)) {
            ivsGateway.stopStream(event.getIvsChannelArn());
        }

        event.markEnded();
        eventRepository.save(event);
        log.info("Evento {} encerrado manualmente pelo customer {}", eventId, customerId);

        auditService.log(customerId, "STREAM_STOPPED", "EVENT", eventId, null);
    }
}
