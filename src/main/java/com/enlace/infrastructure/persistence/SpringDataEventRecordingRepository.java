package com.enlace.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataEventRecordingRepository extends JpaRepository<EventRecordingEntity, UUID> {
    List<EventRecordingEntity> findByEventId(UUID eventId);
}
