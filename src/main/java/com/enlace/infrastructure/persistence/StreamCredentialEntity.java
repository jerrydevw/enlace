package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.StreamCredential;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stream_credentials")
public class StreamCredentialEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "ivs_stream_key_arn", nullable = false)
    private String ivsStreamKeyArn;

    @Column(name = "rtmp_endpoint", nullable = false)
    private String rtmpEndpoint;

    @Column(name = "stream_key", nullable = false)
    private String streamKey;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public StreamCredentialEntity() {}

    public static StreamCredentialEntity fromDomain(StreamCredential credential) {
        StreamCredentialEntity entity = new StreamCredentialEntity();
        entity.id = credential.getId();
        entity.eventId = credential.getEventId();
        entity.ivsStreamKeyArn = credential.getIvsStreamKeyArn();
        entity.rtmpEndpoint = credential.getRtmpEndpoint();
        entity.streamKey = credential.getStreamKey();
        entity.expiresAt = credential.getExpiresAt();
        entity.createdAt = credential.getCreatedAt();
        return entity;
    }

    public StreamCredential toDomain() {
        StreamCredential credential = new StreamCredential();
        credential.setId(id);
        credential.setEventId(eventId);
        credential.setIvsStreamKeyArn(ivsStreamKeyArn);
        credential.setRtmpEndpoint(rtmpEndpoint);
        credential.setStreamKey(streamKey);
        credential.setExpiresAt(expiresAt);
        credential.setCreatedAt(createdAt);
        return credential;
    }
}
