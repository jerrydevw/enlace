package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.out.ViewerTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaViewerTokenRepository implements ViewerTokenRepository {

    private final SpringDataViewerTokenRepository springDataViewerTokenRepository;

    public JpaViewerTokenRepository(SpringDataViewerTokenRepository springDataViewerTokenRepository) {
        this.springDataViewerTokenRepository = springDataViewerTokenRepository;
    }

    @Override
    public ViewerToken save(ViewerToken token) {
        ViewerTokenEntity entity = ViewerTokenEntity.fromDomain(token);
        return springDataViewerTokenRepository.save(entity).toDomain();
    }

    @Override
    public List<ViewerToken> saveAll(List<ViewerToken> tokens) {
        List<ViewerTokenEntity> entities = tokens.stream()
                .map(ViewerTokenEntity::fromDomain)
                .collect(Collectors.toList());
        return springDataViewerTokenRepository.saveAll(entities).stream()
                .map(ViewerTokenEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ViewerToken> findById(UUID id) {
        return springDataViewerTokenRepository.findById(id).map(ViewerTokenEntity::toDomain);
    }

    @Override
    public Optional<ViewerToken> findByToken(String token) {
        return springDataViewerTokenRepository.findByToken(token).map(ViewerTokenEntity::toDomain);
    }

    @Override
    public List<ViewerToken> findByEventId(UUID eventId) {
        return springDataViewerTokenRepository.findByEventId(eventId).stream()
                .map(ViewerTokenEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByEventId(UUID eventId) {
        springDataViewerTokenRepository.deleteByEventId(eventId);
    }
}
