package com.enlace.infrastructure.web.dto;

import java.time.Instant;

public record RecordingResponse(
    String recordingId,
    String filename,
    long sizeBytes,
    Instant recordedAt,
    String downloadUrl
) {}
