package com.enlace.domain.service;

import com.enlace.domain.exception.*;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.ValidateInviteCodeUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerSessionRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import com.enlace.infrastructure.config.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ValidateInviteService implements ValidateInviteCodeUseCase {

    private final EventRepository eventRepository;
    private final ViewerTokenRepository viewerTokenRepository;
    private final ViewerSessionRepository viewerSessionRepository;
    private final JwtService jwtService;

    @Value("${app.rate-limit.max-attempts-per-minute:10}")
    private int maxAttemptsPerMinute;

    @Value("${app.jwt.expiration-hours:4}")
    private int jwtExpirationHours;

    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public ValidateInviteService(EventRepository eventRepository,
                                  ViewerTokenRepository viewerTokenRepository,
                                  ViewerSessionRepository viewerSessionRepository,
                                  JwtService jwtService) {
        this.eventRepository = eventRepository;
        this.viewerTokenRepository = viewerTokenRepository;
        this.viewerSessionRepository = viewerSessionRepository;
        this.jwtService = jwtService;
    }

    @Scheduled(fixedRate = 60000)
    public void clearAttempts() {
        attempts.clear();
    }

    @Override
    @Transactional
    public ViewerSessionResult validate(ValidateInviteCommand command) {
        checkRateLimit(command.ipAddress());

        Event event = eventRepository.findBySlug(command.eventSlug())
                .orElseThrow(() -> new EventNotFoundException("Event not found with slug: " + command.eventSlug()));

        if (event.getStatus() == EventStatus.ENDED) {
            throw new EventEndedException("Event has already ended");
        }

        ViewerToken token = viewerTokenRepository.findByEventSlugAndCode(command.eventSlug(), command.code())
                .orElseThrow(() -> {
                    incrementAttempts(command.ipAddress());
                    return new InvalidInviteCodeException("Invalid invite code");
                });

        if (token.isRevoked()) {
            throw new InviteCodeAlreadyUsedException("Invite code already used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidInviteCodeException("Invite code expired");
        }

        String jti = UUID.randomUUID().toString();
        String sessionToken = jwtService.generateViewerToken(token.getId(), event.getId(), event.getSlug(), jti);
        Instant expiresAt = Instant.now().plus(jwtExpirationHours, ChronoUnit.HOURS);

        ViewerSession session = new ViewerSession();
        session.setId(UUID.randomUUID());
        session.setViewerToken(token);
        session.setEvent(event);
        session.setJti(jti);
        session.setIpAddress(command.ipAddress());
        session.setUserAgent(command.userAgent());
        session.setIssuedAt(Instant.now());
        session.setExpiresAt(expiresAt);
        session.setRevoked(false);

        viewerSessionRepository.save(session);

        token.revoke();
        viewerTokenRepository.save(token);

        return new ViewerSessionResult(
                sessionToken,
                expiresAt,
                event.getTitle(),
                event.getStatus(),
                event.getScheduledAt()
        );
    }

    private void checkRateLimit(String ipAddress) {
        AtomicInteger count = attempts.get(ipAddress);
        if (count != null && count.get() >= maxAttemptsPerMinute) {
            throw new RateLimitExceededException("Too many attempts from this IP");
        }
    }

    private void incrementAttempts(String ipAddress) {
        attempts.computeIfAbsent(ipAddress, k -> new AtomicInteger(0)).incrementAndGet();
    }
}
