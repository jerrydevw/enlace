package com.enlace.domain.port.in;

import java.util.UUID;

public interface ViewerHeartbeatUseCase {
    HeartbeatResult registerHeartbeat(UUID sessionId, UUID eventId, String nonce);
    long countActiveViewers(UUID eventId);

    record HeartbeatResult(String newNonce) {}
}
