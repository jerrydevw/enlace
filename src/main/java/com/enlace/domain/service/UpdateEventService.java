package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.UpdateEventUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.shared.SlugGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UpdateEventService implements UpdateEventUseCase {

    private final EventRepository eventRepository;

    public UpdateEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Event update(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.id())
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + command.id()));

        event.setTitle(command.title());
        event.setScheduledAt(command.scheduledAt());
        
        String baseSlug = SlugGenerator.generate(command.title(), command.scheduledAt());
        String slug = ensureUniqueSlug(baseSlug, event);
        event.setSlug(slug);
        
        event.setUpdatedAt(Instant.now());

        return eventRepository.save(event);
    }

    private String ensureUniqueSlug(String baseSlug, Event currentEvent) {
        String slug = baseSlug;
        int counter = 1;
        while (true) {
            var existingEvent = eventRepository.findBySlug(slug);
            if (existingEvent.isEmpty() || existingEvent.get().getId().equals(currentEvent.getId())) {
                return slug;
            }
            slug = baseSlug + "-" + counter;
            counter++;
        }
    }
}
