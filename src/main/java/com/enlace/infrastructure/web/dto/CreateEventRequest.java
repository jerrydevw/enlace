package com.enlace.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
    UUID customerId,
    @NotBlank String title,
    @NotNull Instant scheduledAt
) {}
