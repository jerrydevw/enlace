package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.ViewerToken;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viewer_tokens")
public class ViewerTokenEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ViewerTokenEntity() {}

    public static ViewerTokenEntity fromDomain(ViewerToken token) {
        ViewerTokenEntity entity = new ViewerTokenEntity();
        entity.id = token.getId();
        entity.eventId = token.getEventId();
        entity.label = token.getLabel();
        entity.token = token.getToken();
        entity.revoked = token.isRevoked();
        entity.expiresAt = token.getExpiresAt();
        entity.createdAt = token.getCreatedAt();
        return entity;
    }

    public ViewerToken toDomain() {
        ViewerToken token = new ViewerToken();
        token.setId(id);
        token.setEventId(eventId);
        token.setLabel(label);
        token.setToken(this.token);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(createdAt);
        return token;
    }
}
