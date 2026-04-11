package com.enlace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRecording {
    private UUID id;
    private UUID eventId;
    private String s3Key;
    private String quality;
    private long durationMs;
    private Instant recordedAt;
}
