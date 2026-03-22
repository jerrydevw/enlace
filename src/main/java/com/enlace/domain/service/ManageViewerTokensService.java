package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.exception.ViewerTokenNotFoundException;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.ManageViewerTokensUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import com.enlace.shared.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageViewerTokensService implements ManageViewerTokensUseCase {

    private final EventRepository eventRepository;
    private final ViewerTokenRepository viewerTokenRepository;

    @Value("${app.viewer-token-ttl-hours:72}")
    private int tokenTtlHours;

    public ManageViewerTokensService(EventRepository eventRepository, ViewerTokenRepository viewerTokenRepository) {
        this.eventRepository = eventRepository;
        this.viewerTokenRepository = viewerTokenRepository;
    }

    @Override
    @Transactional
    public List<ViewerToken> generateTokens(UUID eventId, List<String> labels) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        Instant expiresAt = Instant.now().plus(tokenTtlHours, ChronoUnit.HOURS);

        List<ViewerToken> tokens = labels.stream()
                .map(label -> new ViewerToken(
                        UUID.randomUUID(),
                        eventId,
                        label,
                        TokenGenerator.generate(),
                        expiresAt
                ))
                .collect(Collectors.toList());

        return viewerTokenRepository.saveAll(tokens);
    }

    @Override
    @Transactional
    public void revokeToken(UUID eventId, UUID tokenId) {
        ViewerToken token = viewerTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ViewerTokenNotFoundException("Token not found: " + tokenId));
        
        if (!token.getEventId().equals(eventId)) {
             throw new ViewerTokenNotFoundException("Token does not belong to this event");
        }

        token.revoke();
        viewerTokenRepository.save(token);
    }
}
