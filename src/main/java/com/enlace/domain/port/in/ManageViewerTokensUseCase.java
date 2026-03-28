package com.enlace.domain.port.in;

import com.enlace.domain.model.ViewerToken;
import com.enlace.infrastructure.web.dto.CreateViewerTokenRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ManageViewerTokensUseCase {
    List<ViewerToken> generateTokens(UUID eventId, List<CreateViewerTokenRequest> requests);
    void revokeToken(UUID eventId, UUID tokenId);
    List<ViewerToken> getTokensByEventId(UUID eventId);
    Page<ViewerToken> getTokensByEventId(UUID eventId, Pageable pageable);
}
