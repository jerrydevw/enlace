package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.Plan;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventResponse(
    UUID id,
    String slug,
    String title,
    Instant scheduledAt,
    EventStatus status,
    Plan plan,
    Map<String, Object> planLimits,
    String ivsPlaybackUrl,
    Instant createdAt,
    Instant deletedAt
) {}
