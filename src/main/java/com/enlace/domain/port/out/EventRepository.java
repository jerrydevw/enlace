package com.enlace.domain.port.out;

import com.enlace.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
    Optional<Event> findBySlug(String slug);
    List<Event> findByCustomerId(UUID customerId);
    Page<Event> findByCustomerId(UUID customerId, Pageable pageable);
    void delete(Event event);
}
