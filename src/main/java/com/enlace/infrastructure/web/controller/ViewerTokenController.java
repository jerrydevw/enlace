package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.in.ManageViewerTokensUseCase;
import com.enlace.infrastructure.web.dto.CreateViewerTokenRequest;
import com.enlace.infrastructure.web.dto.ViewerTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events/{id}/tokens")
public class ViewerTokenController {

    private final ManageViewerTokensUseCase manageViewerTokensUseCase;
    private final GetEventUseCase getEventUseCase;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ViewerTokenController(ManageViewerTokensUseCase manageViewerTokensUseCase, GetEventUseCase getEventUseCase) {
        this.manageViewerTokensUseCase = manageViewerTokensUseCase;
        this.getEventUseCase = getEventUseCase;
    }

    @PostMapping
    public ResponseEntity<List<ViewerTokenResponse>> createTokens(@PathVariable UUID id, @RequestBody CreateViewerTokenRequest request) {
        Event event = getEventUseCase.getById(id);
        List<ViewerToken> tokens = manageViewerTokensUseCase.generateTokens(id, request.labels());
        
        List<ViewerTokenResponse> response = tokens.stream()
                .map(token -> new ViewerTokenResponse(
                        token.getId(),
                        token.getLabel(),
                        token.getToken(),
                        String.format("%s/watch/%s?t=%s", baseUrl, event.getSlug(), token.getToken()),
                        token.getExpiresAt()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> revokeToken(@PathVariable UUID id, @PathVariable UUID tokenId) {
        manageViewerTokensUseCase.revokeToken(id, tokenId);
        return ResponseEntity.noContent().build();
    }
}
