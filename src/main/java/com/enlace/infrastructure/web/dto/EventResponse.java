package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.EventStatus;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
    UUID id,
    String slug,
    String title,
    Instant scheduledAt,
    EventStatus status,
    String ivsPlaybackUrl,
    Instant createdAt,
    Instant deletedAt
) {}
