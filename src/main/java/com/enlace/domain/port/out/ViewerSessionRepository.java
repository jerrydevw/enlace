package com.enlace.domain.port.out;

import com.enlace.domain.model.ViewerSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewerSessionRepository {
    ViewerSession save(ViewerSession session);
    Optional<ViewerSession> findByJti(String jti);
    List<ViewerSession> findByEventId(UUID eventId);
    Optional<ViewerSession> findById(UUID id);
    void deleteByEventId(UUID eventId);
}
