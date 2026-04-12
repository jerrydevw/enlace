package com.enlace.domain.service;

import com.enlace.domain.exception.ForbiddenException;
import com.enlace.domain.exception.SessionRevokedException;
import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.port.in.RevokeViewerSessionUseCase;
import com.enlace.domain.port.out.ViewerSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RevokeViewerSessionService implements RevokeViewerSessionUseCase {

    private final ViewerSessionRepository viewerSessionRepository;

    public RevokeViewerSessionService(ViewerSessionRepository viewerSessionRepository) {
        this.viewerSessionRepository = viewerSessionRepository;
    }

    @Override
    @Transactional
    public void revoke(UUID sessionId, UUID requestingCustomerId) {
        ViewerSession session = viewerSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionRevokedException("Session not found"));

        UUID eventOwnerId = session.getEvent().getCustomerId();
        if (!eventOwnerId.equals(requestingCustomerId)) {
            throw new ForbiddenException("Você não tem permissão para revogar esta sessão.");
        }

        session.revoke();
        viewerSessionRepository.save(session);
    }
}
