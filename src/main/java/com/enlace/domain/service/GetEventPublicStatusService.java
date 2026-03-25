package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.GetEventPublicStatusUseCase;
import com.enlace.domain.port.out.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class GetEventPublicStatusService implements GetEventPublicStatusUseCase {

    private final EventRepository eventRepository;

    public GetEventPublicStatusService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventPublicStatus getBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new EventNotFoundException("Event not found with slug: " + slug));

        return new EventPublicStatus(
                event.getTitle(),
                event.getStatus(),
                event.getScheduledAt()
        );
    }
}
