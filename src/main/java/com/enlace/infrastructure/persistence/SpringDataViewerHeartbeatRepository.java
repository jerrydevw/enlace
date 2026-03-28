package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface SpringDataViewerHeartbeatRepository extends JpaRepository<ViewerHeartbeatEntity, UUID> {
    
    long countByEventIdAndLastPingAfter(UUID eventId, Instant threshold);

    @Transactional
    @Modifying
    @Query("DELETE FROM ViewerHeartbeatEntity v WHERE v.lastPing < :threshold")
    void deleteOlderThan(Instant threshold);
}
