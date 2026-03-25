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
public class ViewerSession {
    private UUID id;
    private ViewerToken viewerToken;
    private Event event;
    private String jti;
    private String ipAddress;
    private String userAgent;
    private Instant issuedAt;
    private Instant expiresAt;
    private boolean revoked = false;

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}
