package com.enlace.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ViewerToken {
    private UUID id;
    private UUID eventId;
    private String label;
    private String token;
    private boolean revoked;
    private Instant expiresAt;
    private Instant createdAt;

    public ViewerToken() {}

    public ViewerToken(UUID id, UUID eventId, String label, String token, Instant expiresAt) {
        this.id = id;
        this.eventId = eventId;
        this.label = label;
        this.token = token;
        this.revoked = false;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public void revoke() {
        this.revoked = true;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getLabel() { return label; }
    public String getToken() { return token; }
    public boolean isRevoked() { return revoked; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters for JPA
    public void setId(UUID id) { this.id = id; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public void setLabel(String label) { this.label = label; }
    public void setToken(String token) { this.token = token; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
