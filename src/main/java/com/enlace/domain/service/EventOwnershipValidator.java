package com.enlace.domain.service;

import com.enlace.domain.exception.ForbiddenException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.out.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventOwnershipValidator {

    private final EventRepository eventRepository;

    public void validate(UUID eventId, UUID customerId) {
        Event event = eventRepository.findById(eventId)
                .orElse(null); // Deixa o GetEventService lançar NotFound se necessário, 
                               // mas aqui validamos ownership se ele existir.
        
        if (event != null && !event.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("Você não tem permissão para acessar este recurso.");
        }
    }
}
