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
public class ViewerToken {
    private UUID id;
    private UUID eventId;
    private String label;
    private String token;
    private boolean revoked;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant deletedAt;
 
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
}
