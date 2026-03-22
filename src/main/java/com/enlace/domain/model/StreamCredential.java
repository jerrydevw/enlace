package com.enlace.domain.model;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
 
import java.time.Instant;
import java.util.UUID;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "streamKey")
public class StreamCredential {
    private UUID id;
    private UUID eventId;
    private String ivsStreamKeyArn;
    private String rtmpEndpoint;
    private String streamKey;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant deletedAt;
 
    public StreamCredential(UUID id, UUID eventId, String ivsStreamKeyArn, String rtmpEndpoint, String streamKey, Instant expiresAt) {
        this.id = id;
        this.eventId = eventId;
        this.ivsStreamKeyArn = ivsStreamKeyArn;
        this.rtmpEndpoint = rtmpEndpoint;
        this.streamKey = streamKey;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }
}
