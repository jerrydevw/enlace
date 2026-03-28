package com.enlace.domain.service;

import com.enlace.domain.exception.PlanLimitExceededException;
import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.Plan;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanLimitsServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ViewerTokenRepository viewerTokenRepository;

    @InjectMocks
    private PlanLimitsService planLimitsService;

    private Customer basicCustomer;
    private Customer proCustomer;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        basicCustomer = new Customer(UUID.randomUUID(), "Basic", "basic@example.com", Plan.BASIC, "pass", Instant.now(), null);
        proCustomer = new Customer(UUID.randomUUID(), "Pro", "pro@example.com", Plan.PRO, "pass", Instant.now(), null);
    }

    @Test
    void validateEventCreation_ShouldPass_WhenBelowLimit() {
        when(eventRepository.findByCustomerId(basicCustomer.getId())).thenReturn(List.of());
        assertDoesNotThrow(() -> planLimitsService.validateEventCreation(basicCustomer));
    }

    @Test
    void validateEventCreation_ShouldThrow_WhenAtLimit() {
        Event activeEvent = new Event(UUID.randomUUID(), basicCustomer.getId(), "slug", "Title", Instant.now());
        activeEvent.setStatus(EventStatus.READY);
        
        when(eventRepository.findByCustomerId(basicCustomer.getId())).thenReturn(List.of(activeEvent));
        
        assertThrows(PlanLimitExceededException.class, () -> planLimitsService.validateEventCreation(basicCustomer));
    }

    @Test
    void validateEventCreation_ShouldPass_WhenExistingEventIsEnded() {
        Event endedEvent = new Event(UUID.randomUUID(), basicCustomer.getId(), "slug", "Title", Instant.now());
        endedEvent.setStatus(EventStatus.ENDED);
        
        when(eventRepository.findByCustomerId(basicCustomer.getId())).thenReturn(List.of(endedEvent));
        
        assertDoesNotThrow(() -> planLimitsService.validateEventCreation(basicCustomer));
    }

    @Test
    void validateTokenGeneration_ShouldPass_WhenBelowLimit() {
        when(viewerTokenRepository.findByEventId(eventId)).thenReturn(List.of());
        assertDoesNotThrow(() -> planLimitsService.validateTokenGeneration(eventId, 50, basicCustomer));
    }

    @Test
    void validateTokenGeneration_ShouldThrow_WhenExceedsLimit() {
        when(viewerTokenRepository.findByEventId(eventId)).thenReturn(List.of());
        assertThrows(PlanLimitExceededException.class, () -> planLimitsService.validateTokenGeneration(eventId, 51, basicCustomer));
    }

    @Test
    void validateTokenGeneration_ShouldPass_ForProCustomer() {
        when(viewerTokenRepository.findByEventId(eventId)).thenReturn(List.of());
        assertDoesNotThrow(() -> planLimitsService.validateTokenGeneration(eventId, 500, proCustomer));
    }
}
