package com.enlace.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Event {
    private UUID id;
    private UUID customerId;
    private String slug;
    private String title;
    private Instant scheduledAt;
    private EventStatus status;
    private String ivsChannelArn;
    private String ivsChannelIngestEndpoint;
    private String ivsPlaybackUrl;
    private String recordingS3Prefix;
    private Instant createdAt;
    private Instant updatedAt;

    public Event() {}

    public Event(UUID id, UUID customerId, String slug, String title, Instant scheduledAt) {
        this.id = id;
        this.customerId = customerId;
        this.slug = slug;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.status = EventStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void transitionTo(EventStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + next);
        }
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public void markProvisioning() {
        transitionTo(EventStatus.PROVISIONING);
    }

    public void markReady(String channelArn, String ingestEndpoint, String playbackUrl, String s3Prefix) {
        this.ivsChannelArn = channelArn;
        this.ivsChannelIngestEndpoint = ingestEndpoint;
        this.ivsPlaybackUrl = playbackUrl;
        this.recordingS3Prefix = s3Prefix;
        transitionTo(EventStatus.READY);
    }

    public void markProvisioningFailed() {
        transitionTo(EventStatus.PROVISIONING_FAILED);
    }

    public void markLive() {
        transitionTo(EventStatus.LIVE);
    }

    public void markEnded() {
        transitionTo(EventStatus.ENDED);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public Instant getScheduledAt() { return scheduledAt; }
    public EventStatus getStatus() { return status; }
    public String getIvsChannelArn() { return ivsChannelArn; }
    public String getIvsChannelIngestEndpoint() { return ivsChannelIngestEndpoint; }
    public String getIvsPlaybackUrl() { return ivsPlaybackUrl; }
    public String getRecordingS3Prefix() { return recordingS3Prefix; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters for JPA
    public void setId(UUID id) { this.id = id; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setTitle(String title) { this.title = title; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setIvsChannelArn(String ivsChannelArn) { this.ivsChannelArn = ivsChannelArn; }
    public void setIvsChannelIngestEndpoint(String ivsChannelIngestEndpoint) { this.ivsChannelIngestEndpoint = ivsChannelIngestEndpoint; }
    public void setIvsPlaybackUrl(String ivsPlaybackUrl) { this.ivsPlaybackUrl = ivsPlaybackUrl; }
    public void setRecordingS3Prefix(String recordingS3Prefix) { this.recordingS3Prefix = recordingS3Prefix; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
