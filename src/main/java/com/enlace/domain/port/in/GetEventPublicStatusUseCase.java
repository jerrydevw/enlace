package com.enlace.domain.port.in;

import com.enlace.domain.model.EventStatus;
import java.time.Instant;

public interface GetEventPublicStatusUseCase {
    EventPublicStatus getBySlug(String slug);

    record EventPublicStatus(
        String title,
        EventStatus status,
        Instant scheduledAt
    ) {}
}
