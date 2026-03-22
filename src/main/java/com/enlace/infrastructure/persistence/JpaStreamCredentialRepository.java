package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.out.StreamCredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaStreamCredentialRepository implements StreamCredentialRepository {

    private final SpringDataStreamCredentialRepository springDataStreamCredentialRepository;

    public JpaStreamCredentialRepository(SpringDataStreamCredentialRepository springDataStreamCredentialRepository) {
        this.springDataStreamCredentialRepository = springDataStreamCredentialRepository;
    }

    @Override
    public StreamCredential save(StreamCredential credential) {
        StreamCredentialEntity entity = StreamCredentialEntity.fromDomain(credential);
        return springDataStreamCredentialRepository.save(entity).toDomain();
    }

    @Override
    public Optional<StreamCredential> findByEventId(UUID eventId) {
        return springDataStreamCredentialRepository.findByEventId(eventId).map(StreamCredentialEntity::toDomain);
    }

    @Override
    public void deleteByEventId(UUID eventId) {
        springDataStreamCredentialRepository.deleteByEventId(eventId);
    }
}
