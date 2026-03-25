package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataViewerSessionRepository extends JpaRepository<ViewerSessionEntity, UUID> {
    @EntityGraph(attributePaths = {"viewerToken", "event"})
    Optional<ViewerSessionEntity> findByJti(String jti);
    List<ViewerSessionEntity> findByEventId(UUID eventId);
}
