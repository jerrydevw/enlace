package com.enlace.domain.service;

import com.enlace.domain.exception.SessionConflictException;
import com.enlace.domain.exception.SessionRevokedException;
import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.port.in.ViewerHeartbeatUseCase;
import com.enlace.domain.port.out.ViewerSessionRepository;
import com.enlace.infrastructure.persistence.SpringDataViewerHeartbeatRepository;
import com.enlace.infrastructure.persistence.ViewerHeartbeatEntity;
import com.enlace.shared.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerHeartbeatService implements ViewerHeartbeatUseCase {

    private final SpringDataViewerHeartbeatRepository heartbeatRepository;
    private final ViewerSessionRepository viewerSessionRepository;

    @Override
    @Transactional
    public HeartbeatResult registerHeartbeat(UUID sessionId, UUID eventId, String nonce) {
        ViewerSession session = viewerSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionRevokedException("Session not found"));

        if (session.getActiveNonce() != null && !session.getActiveNonce().equals(nonce)) {
            throw new SessionConflictException("Session already active in another tab or device");
        }

        String newNonce = TokenGenerator.generate();
        session.setActiveNonce(newNonce);
        viewerSessionRepository.save(session);

        ViewerHeartbeatEntity entity = new ViewerHeartbeatEntity(sessionId, eventId, Instant.now());
        heartbeatRepository.save(entity);

        return new HeartbeatResult(newNonce);
    }

    @Override
    public long countActiveViewers(UUID eventId) {
        Instant threshold = Instant.now().minus(60, ChronoUnit.SECONDS);
        return heartbeatRepository.countByEventIdAndLastPingAfter(eventId, threshold);
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanOldHeartbeats() {
        Instant threshold = Instant.now().minus(5, ChronoUnit.MINUTES);
        log.info("Limpando heartbeats mais antigos que {}", threshold);
        heartbeatRepository.deleteOlderThan(threshold);
    }
}
