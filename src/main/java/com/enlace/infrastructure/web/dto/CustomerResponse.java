package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.Plan;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String name,
    String email,
    Plan plan,
    Instant createdAt
) {}
