package com.enlace.domain.port.in;

import com.enlace.domain.model.EventStatus;
import com.enlace.infrastructure.web.dto.CoupleStoryResponse;
import java.time.Instant;

public interface GetEventPublicStatusUseCase {
    EventPublicStatus getBySlug(String slug);

    record EventPublicStatus(
        String title,
        EventStatus status,
        Instant scheduledAt,
        CoupleStoryResponse coupleStory
    ) {}
}
