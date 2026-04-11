package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.EventRecording;
import com.enlace.domain.port.out.EventRecordingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaEventRecordingRepository implements EventRecordingRepository {

    private final SpringDataEventRecordingRepository springDataRepository;

    public JpaEventRecordingRepository(SpringDataEventRecordingRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public EventRecording save(EventRecording recording) {
        return springDataRepository.save(EventRecordingEntity.fromDomain(recording)).toDomain();
    }

    @Override
    public List<EventRecording> findByEventId(UUID eventId) {
        return springDataRepository.findByEventId(eventId).stream()
                .map(EventRecordingEntity::toDomain)
                .collect(Collectors.toList());
    }
}
