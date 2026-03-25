package com.enlace.domain.port.in;

import com.enlace.domain.model.ViewerToken;
import com.enlace.infrastructure.web.dto.CreateViewerTokenRequest;

import java.util.List;
import java.util.UUID;

public interface ManageViewerTokensUseCase {
    List<ViewerToken> generateTokens(UUID eventId, List<CreateViewerTokenRequest> requests);
    void revokeToken(UUID eventId, UUID tokenId);
    List<ViewerToken> getTokensByEventId(UUID eventId);
}
