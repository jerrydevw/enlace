package com.enlace.infrastructure.web.controller;

import com.enlace.domain.port.in.GetEventPublicStatusUseCase;
import com.enlace.domain.port.in.GetPlaybackUrlUseCase;
import com.enlace.domain.port.in.ValidateInviteCodeUseCase;
import com.enlace.domain.port.in.RevokeViewerSessionUseCase;
import com.enlace.infrastructure.config.CustomerAuthentication;
import com.enlace.infrastructure.config.ViewerAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
public class ViewerAccessController {

    private final ValidateInviteCodeUseCase validateInviteCodeUseCase;
    private final GetEventPublicStatusUseCase getEventPublicStatusUseCase;
    private final GetPlaybackUrlUseCase getPlaybackUrlUseCase;
    private final RevokeViewerSessionUseCase revokeViewerSessionUseCase;

    public ViewerAccessController(ValidateInviteCodeUseCase validateInviteCodeUseCase,
                                  GetEventPublicStatusUseCase getEventPublicStatusUseCase,
                                  GetPlaybackUrlUseCase getPlaybackUrlUseCase,
                                  RevokeViewerSessionUseCase revokeViewerSessionUseCase) {
        this.validateInviteCodeUseCase = validateInviteCodeUseCase;
        this.getEventPublicStatusUseCase = getEventPublicStatusUseCase;
        this.getPlaybackUrlUseCase = getPlaybackUrlUseCase;
        this.revokeViewerSessionUseCase = revokeViewerSessionUseCase;
    }

    @PostMapping("/{slug}/access")
    public ResponseEntity<ValidateInviteCodeUseCase.ViewerSessionResult> access(
            @PathVariable String slug,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        
        String code = body.get("inviteCode");
        String inviteToken = body.get("inviteToken");
        log.info("Tentativa de acesso ao evento: {}", slug);

        ValidateInviteCodeUseCase.ViewerSessionResult result = validateInviteCodeUseCase.validate(
                new ValidateInviteCodeUseCase.ValidateInviteCommand(
                        slug,
                        code,
                        inviteToken,
                        extractIp(request),
                        request.getHeader("User-Agent")
                )
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}/public-status")
    public ResponseEntity<GetEventPublicStatusUseCase.EventPublicStatus> getPublicStatus(@PathVariable String slug) {
        return ResponseEntity.ok(getEventPublicStatusUseCase.getBySlug(slug));
    }

    @GetMapping("/{slug}/playback-url")
    public ResponseEntity<Map<String, Object>> getPlaybackUrl(
            @PathVariable String slug,
            ViewerAuthentication auth) {
        
        if (auth == null) {
            log.warn("Tentativa de acesso ao playback-url sem autenticação para o slug: {}", slug);
            return ResponseEntity.status(401).build();
        }
        
        String playbackUrl = getPlaybackUrlUseCase.getPlaybackUrl(null, auth.getJti());
        GetEventPublicStatusUseCase.EventPublicStatus status = getEventPublicStatusUseCase.getBySlug(slug);

        return ResponseEntity.ok(Map.of(
                "playbackUrl", playbackUrl,
                "eventStatus", status.status()
        ));
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomerAuthentication auth) {
        revokeViewerSessionUseCase.revoke(sessionId, auth.getCustomerId());
        return ResponseEntity.noContent().build();
    }
}
