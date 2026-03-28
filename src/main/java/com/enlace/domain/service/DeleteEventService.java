package com.enlace.domain.service;

import com.enlace.domain.exception.EventDeletionNotAllowedException;
import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.DeleteEventUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.IvsGateway;
import com.enlace.domain.port.out.StreamCredentialRepository;
import com.enlace.domain.port.out.ViewerSessionRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteEventService implements DeleteEventUseCase {

    private final EventRepository eventRepository;
    private final StreamCredentialRepository streamCredentialRepository;
    private final ViewerTokenRepository viewerTokenRepository;
    private final ViewerSessionRepository viewerSessionRepository;
    private final IvsGateway ivsGateway;

    public DeleteEventService(EventRepository eventRepository, StreamCredentialRepository streamCredentialRepository, ViewerTokenRepository viewerTokenRepository, ViewerSessionRepository viewerSessionRepository, IvsGateway ivsGateway) {
        this.eventRepository = eventRepository;
        this.streamCredentialRepository = streamCredentialRepository;
        this.viewerTokenRepository = viewerTokenRepository;
        this.viewerSessionRepository = viewerSessionRepository;
        this.ivsGateway = ivsGateway;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + id));

        if (event.getStatus() == EventStatus.LIVE) {
            throw new EventDeletionNotAllowedException("Cannot delete a live event");
        }

        if (event.getIvsChannelArn() != null) {
            StreamCredential credential = streamCredentialRepository.findByEventId(id).orElse(null);
            String streamKeyArn = (credential != null) ? credential.getIvsStreamKeyArn() : null;
            ivsGateway.deleteChannel(event.getIvsChannelArn(), streamKeyArn);
        }

        viewerSessionRepository.deleteByEventId(id);
        streamCredentialRepository.deleteByEventId(id);
        viewerTokenRepository.deleteByEventId(id);
        eventRepository.delete(event);
    }
}
