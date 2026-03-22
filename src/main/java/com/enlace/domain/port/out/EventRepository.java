package com.enlace.domain.port.out;

import com.enlace.domain.model.Event;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
    Optional<Event> findBySlug(String slug);
    void delete(Event event);
}
