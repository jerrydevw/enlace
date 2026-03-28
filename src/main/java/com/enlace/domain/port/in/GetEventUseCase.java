package com.enlace.domain.port.in;

import com.enlace.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetEventUseCase {
    Event getById(UUID id);
    Optional<Event> getBySlug(String slug);
    List<Event> listByCustomerId(UUID customerId);
    Page<Event> listByCustomerId(UUID customerId, Pageable pageable);
}
