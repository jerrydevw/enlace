package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.ViewerSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viewer_sessions")
@Getter
@Setter
@NoArgsConstructor
public class ViewerSessionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_token_id", nullable = false)
    private ViewerTokenEntity viewerToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(nullable = false, unique = true)
    private String jti;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "active_nonce", length = 64)
    private String activeNonce;

    public static ViewerSessionEntity fromDomain(ViewerSession session) {
        ViewerSessionEntity entity = new ViewerSessionEntity();
        entity.id = session.getId();
        entity.viewerToken = ViewerTokenEntity.fromDomain(session.getViewerToken());
        entity.event = EventEntity.fromDomain(session.getEvent());
        entity.jti = session.getJti();
        entity.ipAddress = session.getIpAddress();
        entity.userAgent = session.getUserAgent();
        entity.issuedAt = session.getIssuedAt();
        entity.expiresAt = session.getExpiresAt();
        entity.revoked = session.isRevoked();
        entity.activeNonce = session.getActiveNonce();
        return entity;
    }

    public ViewerSession toDomain() {
        ViewerSession session = new ViewerSession();
        session.setId(id);
        session.setViewerToken(viewerToken.toDomain());
        session.setEvent(event.toDomain());
        session.setJti(jti);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setIssuedAt(issuedAt);
        session.setExpiresAt(expiresAt);
        session.setRevoked(revoked);
        session.setActiveNonce(activeNonce);
        return session;
    }
}
