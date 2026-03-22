package com.enlace.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ViewerTokenResponse(
    UUID id,
    String label,
    String token,
    String viewerUrl,
    Instant expiresAt
) {}
