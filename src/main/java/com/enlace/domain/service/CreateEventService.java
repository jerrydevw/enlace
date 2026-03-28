package com.enlace.domain.service;

import com.enlace.domain.exception.CustomerNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.CreateEventUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ProvisioningPublisher;
import com.enlace.shared.SlugGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateEventService implements CreateEventUseCase {

    private final EventRepository eventRepository;
    private final CustomerRepository customerRepository;
    private final ProvisioningPublisher provisioningPublisher;
    private final PlanLimitsService planLimitsService;
    private final AuditService auditService;

    public CreateEventService(EventRepository eventRepository, 
                            CustomerRepository customerRepository, 
                            ProvisioningPublisher provisioningPublisher,
                            PlanLimitsService planLimitsService,
                            AuditService auditService) {
        this.eventRepository = eventRepository;
        this.customerRepository = customerRepository;
        this.provisioningPublisher = provisioningPublisher;
        this.planLimitsService = planLimitsService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Event create(CreateEventCommand command) {
        var customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + command.customerId()));
        
        planLimitsService.validateEventCreation(customer);

        String baseSlug = SlugGenerator.generate(command.title(), command.scheduledAt());
        String slug = ensureUniqueSlug(baseSlug);

        Event event = new Event(
                UUID.randomUUID(),
                command.customerId(),
                slug,
                command.title(),
                command.scheduledAt()
        );

        Event savedEvent = eventRepository.saveAndFlush(event);
        provisioningPublisher.publishProvisioningJob(savedEvent.getId());

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
