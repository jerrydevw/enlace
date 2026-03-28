package com.enlace.domain.service;

import com.enlace.domain.exception.PlanLimitExceededException;
import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
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

    public void validateEventCreation(Customer customer) {
        List<Event> events = eventRepository.findByCustomerId(customer.getId());
        
        long activeEventsCount = events.stream()
                .filter(e -> e.getDeletedAt() == null && e.getStatus() != EventStatus.ENDED)
                .count();

        if (activeEventsCount >= customer.getPlan().getMaxActiveEvents()) {
            throw new PlanLimitExceededException("Limite de eventos ativos excedido para o plano " + customer.getPlan());
        }
    }

    public void validateTokenGeneration(UUID eventId, int requestedCount, Customer customer) {
        List<ViewerToken> existingTokens = viewerTokenRepository.findByEventId(eventId);
        
        long nonRevokedTokens = existingTokens.stream()
                .filter(t -> !t.isRevoked() && t.getDeletedAt() == null)
                .count();

        if (nonRevokedTokens + requestedCount > customer.getPlan().getMaxTokensPerEvent()) {
            throw new PlanLimitExceededException("Limite de tokens por evento excedido para o plano " + customer.getPlan());
        }
    }
}
