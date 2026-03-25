package com.enlace.infrastructure.persistence;
 
import com.enlace.domain.model.ViewerToken;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
 
import java.time.Instant;
import java.util.UUID;
 
@Entity
@Table(name = "viewer_tokens")
@Getter
@Setter
@NoArgsConstructor
public class ViewerTokenEntity {
 
    @Id
    private UUID id;
 
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
 
    @Column(nullable = false)
    private String label;
 
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private ViewerToken.DeliveryStatus deliveryStatus;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_contact")
    private String guestContact;

    @Column(nullable = false)
    private boolean revoked;
 
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted_at")
    @Column(name = "deleted_at")
    private Instant deletedAt;
 
    public static ViewerTokenEntity fromDomain(ViewerToken token) {
        ViewerTokenEntity entity = new ViewerTokenEntity();
        entity.id = token.getId();
        entity.eventId = token.getEventId();
        entity.label = token.getLabel();
        entity.token = token.getToken();
        entity.code = token.getCode();
        entity.deliveryStatus = token.getDeliveryStatus();
        entity.sentAt = token.getSentAt();
        entity.guestName = token.getGuestName();
        entity.guestContact = token.getGuestContact();
        entity.revoked = token.isRevoked();
        entity.expiresAt = token.getExpiresAt();
        entity.createdAt = token.getCreatedAt();
        entity.deletedAt = token.getDeletedAt();
        return entity;
    }

    public ViewerToken toDomain() {
        ViewerToken token = new ViewerToken();
        token.setId(id);
        token.setEventId(eventId);
        token.setLabel(label);
        token.setToken(this.token);
        token.setCode(code);
        token.setDeliveryStatus(deliveryStatus);
        token.setSentAt(sentAt);
        token.setGuestName(guestName);
        token.setGuestContact(guestContact);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(createdAt);
        token.setDeletedAt(deletedAt);
        return token;
    }
}
