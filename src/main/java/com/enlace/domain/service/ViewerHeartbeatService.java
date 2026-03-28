package com.enlace.domain.service;

import com.enlace.domain.port.in.ViewerHeartbeatUseCase;
import com.enlace.infrastructure.persistence.SpringDataViewerHeartbeatRepository;
import com.enlace.infrastructure.persistence.ViewerHeartbeatEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerHeartbeatService implements ViewerHeartbeatUseCase {

    private final SpringDataViewerHeartbeatRepository heartbeatRepository;

    @Override
    public void registerHeartbeat(UUID sessionId, UUID eventId) {
        ViewerHeartbeatEntity entity = new ViewerHeartbeatEntity(sessionId, eventId, Instant.now());
        heartbeatRepository.save(entity);
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
