package com.enlace.domain.port.in;

import com.enlace.domain.model.Event;
import java.time.Instant;
import java.util.UUID;

public interface CreateEventUseCase {
    Event create(CreateEventCommand command);

    record CreateEventCommand(
        UUID customerId,
        String title,
        Instant scheduledAt
    ) {}
}
