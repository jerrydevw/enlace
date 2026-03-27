package com.enlace.domain.service;

import com.enlace.domain.exception.InviteCodeAlreadyUsedException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.ValidateInviteCodeUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerSessionRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import com.enlace.infrastructure.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateInviteServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ViewerTokenRepository viewerTokenRepository;
    @Mock
    private ViewerSessionRepository viewerSessionRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ValidateInviteService validateInviteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(validateInviteService, "maxAttemptsPerMinute", 10);
        ReflectionTestUtils.setField(validateInviteService, "jwtExpirationHours", 4);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsAlreadyRevoked() {
        // Given
        String slug = "test-event";
        String code = "123456";
        ValidateInviteCodeUseCase.ValidateInviteCommand command = new ValidateInviteCodeUseCase.ValidateInviteCommand(
                slug, code, "127.0.0.1", "test-agent"
        );

        Event event = new Event();
        event.setSlug(slug);
        event.setStatus(EventStatus.LIVE);

        ViewerToken token = new ViewerToken();
        token.setCode(code);
        token.setRevoked(true); // Token already used/revoked

        when(eventRepository.findBySlug(slug)).thenReturn(Optional.of(event));
        when(viewerTokenRepository.findByEventSlugAndCode(slug, code)).thenReturn(Optional.of(token));

        // When & Then
        assertThrows(InviteCodeAlreadyUsedException.class, () -> validateInviteService.validate(command));
        verify(viewerSessionRepository, never()).save(any());
    }

    @Test
    void shouldRevokeTokenAfterSuccessfulValidation() {
        // Given
        String slug = "test-event";
        String code = "123456";
        ValidateInviteCodeUseCase.ValidateInviteCommand command = new ValidateInviteCodeUseCase.ValidateInviteCommand(
                slug, code, "127.0.0.1", "test-agent"
        );

        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setSlug(slug);
        event.setStatus(EventStatus.LIVE);

        ViewerToken token = spy(new ViewerToken());
        token.setId(UUID.randomUUID());
        token.setCode(code);
        token.setRevoked(false);
        token.setExpiresAt(Instant.now().plusSeconds(3600));

        when(eventRepository.findBySlug(slug)).thenReturn(Optional.of(event));
        when(viewerTokenRepository.findByEventSlugAndCode(slug, code)).thenReturn(Optional.of(token));
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("test-jwt");

        // When
        validateInviteService.validate(command);

        // Then
        verify(token).revoke();
        verify(viewerTokenRepository).save(token);
        verify(viewerSessionRepository).save(any());
    }
}
