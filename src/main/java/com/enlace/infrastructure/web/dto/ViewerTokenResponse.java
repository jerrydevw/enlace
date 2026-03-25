package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.ViewerToken;
import java.time.Instant;
import java.util.UUID;

public record ViewerTokenResponse(
    UUID id,
    String label,
    String code,
    String guestName,
    ViewerToken.DeliveryStatus deliveryStatus,
    boolean revoked,
    Instant expiresAt,
    Instant deletedAt
) {}
