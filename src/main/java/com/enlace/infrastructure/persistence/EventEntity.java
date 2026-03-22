package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "ivs_channel_arn")
    private String ivsChannelArn;

    @Column(name = "ivs_channel_ingest_endpoint")
    private String ivsChannelIngestEndpoint;

    @Column(name = "ivs_playback_url")
    private String ivsPlaybackUrl;

    @Column(name = "recording_s3_prefix")
    private String recordingS3Prefix;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EventEntity() {}

    public static EventEntity fromDomain(Event event) {
        EventEntity entity = new EventEntity();
        entity.id = event.getId();
        entity.customerId = event.getCustomerId();
        entity.slug = event.getSlug();
        entity.title = event.getTitle();
        entity.scheduledAt = event.getScheduledAt();
        entity.status = event.getStatus();
        entity.ivsChannelArn = event.getIvsChannelArn();
        entity.ivsChannelIngestEndpoint = event.getIvsChannelIngestEndpoint();
        entity.ivsPlaybackUrl = event.getIvsPlaybackUrl();
        entity.recordingS3Prefix = event.getRecordingS3Prefix();
        entity.createdAt = event.getCreatedAt();
        entity.updatedAt = event.getUpdatedAt();
        return entity;
    }

    public Event toDomain() {
        Event event = new Event();
        event.setId(id);
        event.setCustomerId(customerId);
        event.setSlug(slug);
        event.setTitle(title);
        event.setScheduledAt(scheduledAt);
        event.setStatus(status);
        event.setIvsChannelArn(ivsChannelArn);
        event.setIvsChannelIngestEndpoint(ivsChannelIngestEndpoint);
        event.setIvsPlaybackUrl(ivsPlaybackUrl);
        event.setRecordingS3Prefix(recordingS3Prefix);
        event.setCreatedAt(createdAt);
        event.setUpdatedAt(updatedAt);
        return event;
    }
}
