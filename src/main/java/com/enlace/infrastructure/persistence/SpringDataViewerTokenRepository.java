package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataViewerTokenRepository extends JpaRepository<ViewerTokenEntity, UUID> {
    Optional<ViewerTokenEntity> findByToken(String token);
    List<ViewerTokenEntity> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);

    @org.springframework.data.jpa.repository.Query("SELECT vt FROM ViewerTokenEntity vt JOIN EventEntity e ON vt.eventId = e.id WHERE e.slug = :slug AND vt.code = :code")
    Optional<ViewerTokenEntity> findByEventSlugAndCode(String slug, String code);
    
    boolean existsByCode(String code);
}
