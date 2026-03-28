package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.port.out.ViewerSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaViewerSessionRepository implements ViewerSessionRepository {

    private final SpringDataViewerSessionRepository springDataViewerSessionRepository;

    public JpaViewerSessionRepository(SpringDataViewerSessionRepository springDataViewerSessionRepository) {
        this.springDataViewerSessionRepository = springDataViewerSessionRepository;
    }

    @Override
    public ViewerSession save(ViewerSession session) {
        ViewerSessionEntity entity = ViewerSessionEntity.fromDomain(session);
        return springDataViewerSessionRepository.save(entity).toDomain();
    }

    @Override
    public Optional<ViewerSession> findByJti(String jti) {
        return springDataViewerSessionRepository.findByJti(jti).map(ViewerSessionEntity::toDomain);
    }

    @Override
    public List<ViewerSession> findByEventId(UUID eventId) {
        return springDataViewerSessionRepository.findByEventId(eventId).stream()
                .map(ViewerSessionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ViewerSession> findById(UUID id) {
        return springDataViewerSessionRepository.findById(id).map(ViewerSessionEntity::toDomain);
    }

    @Override
    public void deleteByEventId(UUID eventId) {
        springDataViewerSessionRepository.deleteByEventId(eventId);
    }
}
