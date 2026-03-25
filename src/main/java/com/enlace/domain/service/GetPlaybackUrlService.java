package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.exception.EventNotLiveException;
import com.enlace.domain.exception.SessionRevokedException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.port.in.GetPlaybackUrlUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetPlaybackUrlService implements GetPlaybackUrlUseCase {

    private final ViewerSessionRepository viewerSessionRepository;
    private final EventRepository eventRepository;

    public GetPlaybackUrlService(ViewerSessionRepository viewerSessionRepository, EventRepository eventRepository) {
        this.viewerSessionRepository = viewerSessionRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public String getPlaybackUrl(UUID sessionId, String jti) {
        ViewerSession session = viewerSessionRepository.findByJti(jti)
                .orElseThrow(() -> new SessionRevokedException("Session not found"));

        if (!session.isValid()) {
            throw new SessionRevokedException("Session revoked or expired");
        }

        Event event = eventRepository.findById(session.getEvent().getId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.CREATED || event.getStatus() == EventStatus.PROVISIONING) {
            throw new EventNotLiveException("Event is not live yet");
        }

        if (event.getStatus() == EventStatus.ENDED) {
            throw new EventNotLiveException("Event has ended");
        }

        return event.getIvsPlaybackUrl();
    }
}
