package com.enlace.domain.service;

import com.enlace.domain.exception.CustomerNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.CreateEventUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.shared.SlugGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class CreateEventService implements CreateEventUseCase {

    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlanLimitsService planLimitsService;
    private final AuditService auditService;

    public CreateEventService(EventRepository eventRepository, 
                            CustomerRepository customerRepository, 
                            ApplicationEventPublisher eventPublisher,
                            PlanLimitsService planLimitsService,
                            AuditService auditService) {
        this.eventRepository = eventRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.planLimitsService = planLimitsService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Event create(CreateEventCommand command) {
        customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + command.customerId()));
        
        String baseSlug = SlugGenerator.generate(command.title(), command.scheduledAt());
        String slug = ensureUniqueSlug(baseSlug);

        Event event = new Event(
                UUID.randomUUID(),
                command.customerId(),
                slug,
                command.title(),
                command.scheduledAt(),
                command.plan()
        );

        Event savedEvent = eventRepository.saveAndFlush(event);
        log.info("Evento salvo com sucesso: {}", savedEvent.getId());
        eventPublisher.publishEvent(new EventCreatedEvent(savedEvent.getId()));

        auditService.log(command.customerId(), "EVENT_CREATED", "EVENT", savedEvent.getId(), null);

        return savedEvent;
    }

    private String ensureUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;
        while (eventRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
}
