package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.in.ManageViewerTokensUseCase;
import com.enlace.domain.service.EventOwnershipValidator;
import com.enlace.infrastructure.config.CustomerAuthentication;
import com.enlace.infrastructure.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events/{id}/tokens")
@Tag(name = "Viewer Tokens", description = "Endpoints para gestão de tokens de acesso para convidados (viewers).")
public class ViewerTokenController {

    private final ManageViewerTokensUseCase manageViewerTokensUseCase;
    private final GetEventUseCase getEventUseCase;
    private final EventOwnershipValidator ownershipValidator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ViewerTokenController(ManageViewerTokensUseCase manageViewerTokensUseCase, 
                               GetEventUseCase getEventUseCase,
                               EventOwnershipValidator ownershipValidator) {
        this.manageViewerTokensUseCase = manageViewerTokensUseCase;
        this.getEventUseCase = getEventUseCase;
        this.ownershipValidator = ownershipValidator;
    }

    @PostMapping
    @Operation(summary = "Gerar tokens para convidados", description = "Gera um ou mais tokens de acesso para convidados de um evento.")
    public ResponseEntity<List<ViewerTokenResponse>> createTokens(
            @PathVariable UUID id, 
            @RequestBody List<CreateViewerTokenRequest> requests,
            @AuthenticationPrincipal CustomerAuthentication auth) {
        ownershipValidator.validate(id, auth.getCustomerId());
        getEventUseCase.getById(id);
        List<ViewerToken> tokens = manageViewerTokensUseCase.generateTokens(id, requests);
        
        List<ViewerTokenResponse> response = tokens.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tokens do evento", description = "Retorna os tokens gerados para um evento específico com suporte a paginação.")
    public ResponseEntity<PagedResponse<ViewerTokenResponse>> getTokens(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomerAuthentication auth,
            @PageableDefault(size = 50) Pageable pageable) {
        ownershipValidator.validate(id, auth.getCustomerId());
        getEventUseCase.getById(id);
        Page<ViewerToken> tokensPage = manageViewerTokensUseCase.getTokensByEventId(id, pageable);
        
        List<ViewerTokenResponse> content = tokensPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new PagedResponse<>(
                content,
                tokensPage.getNumber(),
                tokensPage.getSize(),
                tokensPage.getTotalElements(),
                tokensPage.getTotalPages()
        ));
    }

    @DeleteMapping("/{tokenId}")
    @Operation(summary = "Revogar token", description = "Invalida um token de acesso para que não possa mais ser usado.")
    public ResponseEntity<Void> revokeToken(
            @PathVariable UUID id, 
            @PathVariable UUID tokenId,
            @AuthenticationPrincipal CustomerAuthentication auth) {
        ownershipValidator.validate(id, auth.getCustomerId());
        manageViewerTokensUseCase.revokeToken(id, tokenId);
        return ResponseEntity.noContent().build();
    }

    private ViewerTokenResponse toResponse(ViewerToken token) {
        return new ViewerTokenResponse(
                token.getId(),
                token.getLabel(),
                token.getCode(),
                token.getGuestName(),
                token.getDeliveryStatus(),
                token.isRevoked(),
                token.getExpiresAt(),
                token.getDeletedAt()
        );
    }
}
