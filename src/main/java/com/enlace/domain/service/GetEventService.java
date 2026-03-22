package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.out.EventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetEventService implements GetEventUseCase {

    private final EventRepository eventRepository;

    public GetEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event getById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
    }
}
