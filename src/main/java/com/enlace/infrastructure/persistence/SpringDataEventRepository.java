package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findBySlug(String slug);
}
