package com.enlace.domain.model;

import java.time.Instant;
import java.util.UUID;

public class StreamCredential {
    private UUID id;
    private UUID eventId;
    private String ivsStreamKeyArn;
    private String rtmpEndpoint;
    private String streamKey;
    private Instant expiresAt;
    private Instant createdAt;

    public StreamCredential() {}

    public StreamCredential(UUID id, UUID eventId, String ivsStreamKeyArn, String rtmpEndpoint, String streamKey, Instant expiresAt) {
        this.id = id;
        this.eventId = eventId;
        this.ivsStreamKeyArn = ivsStreamKeyArn;
        this.rtmpEndpoint = rtmpEndpoint;
        this.streamKey = streamKey;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getIvsStreamKeyArn() { return ivsStreamKeyArn; }
    public String getRtmpEndpoint() { return rtmpEndpoint; }
    public String getStreamKey() { return streamKey; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "StreamCredential{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", ivsStreamKeyArn='" + ivsStreamKeyArn + '\'' +
                ", rtmpEndpoint='" + rtmpEndpoint + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                '}';
    }

    // Setters for JPA
    public void setId(UUID id) { this.id = id; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public void setIvsStreamKeyArn(String ivsStreamKeyArn) { this.ivsStreamKeyArn = ivsStreamKeyArn; }
    public void setRtmpEndpoint(String rtmpEndpoint) { this.rtmpEndpoint = rtmpEndpoint; }
    public void setStreamKey(String streamKey) { this.streamKey = streamKey; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
