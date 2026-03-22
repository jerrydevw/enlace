package com.enlace.domain.port.in;

import com.enlace.domain.model.ViewerToken;
import java.util.List;
import java.util.UUID;

public interface ManageViewerTokensUseCase {
    List<ViewerToken> generateTokens(UUID eventId, List<String> labels);
    void revokeToken(UUID eventId, UUID tokenId);
}
