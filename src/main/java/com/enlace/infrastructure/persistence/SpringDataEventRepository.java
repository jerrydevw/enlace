package com.enlace.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findBySlug(String slug);
    List<EventEntity> findByCustomerId(UUID customerId);
    Page<EventEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
