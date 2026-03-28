package com.enlace.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viewer_heartbeats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewerHeartbeatEntity {

    @Id
    private UUID sessionId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "last_ping", nullable = false)
    private Instant lastPing;
}
