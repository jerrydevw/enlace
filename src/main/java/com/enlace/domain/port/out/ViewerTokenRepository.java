package com.enlace.domain.port.out;

import com.enlace.domain.model.ViewerToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewerTokenRepository {
    ViewerToken save(ViewerToken token);
    List<ViewerToken> saveAll(List<ViewerToken> tokens);
    Optional<ViewerToken> findById(UUID id);
    Optional<ViewerToken> findByToken(String token);
    List<ViewerToken> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);
}
