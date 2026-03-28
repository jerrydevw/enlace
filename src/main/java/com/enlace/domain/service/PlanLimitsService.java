package com.enlace.domain.service;

import com.enlace.domain.exception.PlanLimitExceededException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanLimitsService {

    private final EventRepository eventRepository;
    private final ViewerTokenRepository viewerTokenRepository;

    public void validateViewerLimit(Event event, int currentViewers) {
        if (currentViewers >= event.getPlan().getMaxViewersPerEvent()) {
            throw new PlanLimitExceededException(
                "Limite de " + event.getPlan().getMaxViewersPerEvent() + 
                " espectadores atingido para o plano " + event.getPlan()
            );
        }
    }

    public void validateTokenGeneration(UUID eventId, int requestedCount) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado: " + eventId));
                
        List<ViewerToken> existingTokens = viewerTokenRepository.findByEventId(eventId);
        
        long nonRevokedTokens = existingTokens.stream()
                .filter(t -> !t.isRevoked() && t.getDeletedAt() == null)
                .count();

        if (nonRevokedTokens + requestedCount > event.getPlan().getMaxViewersPerEvent()) {
            throw new PlanLimitExceededException("Limite de tokens por evento excedido para o plano " + event.getPlan());
        }
    }
}
