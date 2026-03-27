package com.enlace.domain.port.in;

import com.enlace.domain.model.Event;
import java.time.Instant;
import java.util.UUID;

public interface UpdateEventUseCase {
    Event update(UpdateEventCommand command);

    record UpdateEventCommand(
        UUID id,
        String title,
        Instant scheduledAt
    ) {}
}
