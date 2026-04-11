package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.EventRecording;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_recordings")
@Getter
@Setter
@NoArgsConstructor
public class EventRecordingEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "quality", nullable = false)
    private String quality;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    public static EventRecordingEntity fromDomain(EventRecording recording) {
        EventRecordingEntity entity = new EventRecordingEntity();
        entity.id = recording.getId();
        entity.eventId = recording.getEventId();
        entity.s3Key = recording.getS3Key();
        entity.quality = recording.getQuality();
        entity.durationMs = recording.getDurationMs();
        entity.recordedAt = recording.getRecordedAt();
        return entity;
    }

    public EventRecording toDomain() {
        return new EventRecording(id, eventId, s3Key, quality, durationMs, recordedAt);
    }
}
