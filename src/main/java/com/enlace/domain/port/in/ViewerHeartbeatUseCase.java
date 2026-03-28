package com.enlace.domain.port.in;

import java.util.UUID;

public interface ViewerHeartbeatUseCase {
    void registerHeartbeat(UUID sessionId, UUID eventId);
    long countActiveViewers(UUID eventId);
}
