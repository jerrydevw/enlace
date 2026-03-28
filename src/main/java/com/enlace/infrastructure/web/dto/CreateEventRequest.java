package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.Plan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * DTO para criação de evento.
 * customerId NÃO vem no payload - é extraído do token JWT.
 * plan é OBRIGATÓRIO - usuário escolhe o plano para cada evento.
 */
public record CreateEventRequest(
    @NotBlank String title,
    @NotNull Instant scheduledAt,
    @NotNull Plan plan
) {}
