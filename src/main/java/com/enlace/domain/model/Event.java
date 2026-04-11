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
public class Event {
    private UUID id;
    private UUID customerId;
    private String slug;
    private String title;
    private Instant scheduledAt;
    private EventStatus status;
    private Plan plan;
    private String ivsChannelArn;
    private String ivsChannelIngestEndpoint;
    private String ivsPlaybackUrl;
    private String recordingS3Prefix;
    private boolean recordingAvailable;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private CoupleStory coupleStory;   // nullable — null = sem personalização
 
    public Event(UUID id, UUID customerId, String slug, String title, Instant scheduledAt, Plan plan) {
        this.id = id;
        this.customerId = customerId;
        this.slug = slug;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.plan = plan;
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

    public void markRecordingAvailable() {
        this.recordingAvailable = true;
        this.updatedAt = Instant.now();
    }
}
