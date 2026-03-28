package com.enlace.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    CustomerResponse customer
) {
    public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String plan,
        Instant createdAt
    ) {}
}
