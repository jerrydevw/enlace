package com.enlace.domain.service;

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
    public void revoke(UUID sessionId) {
        ViewerSession session = viewerSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionRevokedException("Session not found"));
        
        session.revoke();
        viewerSessionRepository.save(session);
    }
}
