package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.out.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<Event> getBySlug(String slug) {
        return eventRepository.findBySlug(slug);
    }

    @Override
    public List<Event> listByCustomerId(UUID customerId) {
        return eventRepository.findByCustomerId(customerId);
    }

    @Override
    public Page<Event> listByCustomerId(UUID customerId, Pageable pageable) {
        return eventRepository.findByCustomerId(customerId, pageable);
    }
}
