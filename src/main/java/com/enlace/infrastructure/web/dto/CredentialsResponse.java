package com.enlace.infrastructure.web.dto;

import java.time.Instant;

public record CredentialsResponse(
    String rtmpEndpoint,
    String streamKey,
    Instant expiresAt
) {}
