package com.enlace.domain.port.in;

import java.util.UUID;

public interface GetPlaybackUrlUseCase {
    String getPlaybackUrl(UUID sessionId, String jti);
}
