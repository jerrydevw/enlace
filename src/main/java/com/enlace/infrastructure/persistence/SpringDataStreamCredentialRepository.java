package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataStreamCredentialRepository extends JpaRepository<StreamCredentialEntity, UUID> {
    Optional<StreamCredentialEntity> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);
}
