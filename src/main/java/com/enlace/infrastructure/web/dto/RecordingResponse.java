package com.enlace.infrastructure.web.dto;

import java.time.Instant;

public record RecordingResponse(
    String recordingId,
    String quality,
    long durationMs,
    Instant recordedAt,
    String downloadUrl
) {}
