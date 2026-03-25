package com.enlace.domain.port.in;

import java.util.UUID;

public interface RevokeViewerSessionUseCase {
    void revoke(UUID sessionId);
}
