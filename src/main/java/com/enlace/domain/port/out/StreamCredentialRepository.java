package com.enlace.domain.port.out;

import com.enlace.domain.model.StreamCredential;
import java.util.Optional;
import java.util.UUID;

public interface StreamCredentialRepository {
    StreamCredential save(StreamCredential credential);
    Optional<StreamCredential> findByEventId(UUID eventId);
    void deleteByEventId(UUID eventId);
}
