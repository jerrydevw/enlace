package com.enlace.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateEventRequest(
    @NotBlank String title,
    @NotNull Instant scheduledAt
) {}
