package com.enlace.domain.service;

import com.enlace.domain.port.out.ProvisioningPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class EventCreatedListener {

    private final ProvisioningPublisher provisioningPublisher;

    public EventCreatedListener(ProvisioningPublisher provisioningPublisher) {
        this.provisioningPublisher = provisioningPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEventCreated(EventCreatedEvent event) {
        log.info("Processando evento criado após commit para provisionamento: {}", event.eventId());
        provisioningPublisher.publishProvisioningJob(event.eventId());
    }
}
