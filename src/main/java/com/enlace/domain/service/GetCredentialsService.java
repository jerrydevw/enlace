package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.exception.EventNotReadyException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.GetCredentialsUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.StreamCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCredentialsService implements GetCredentialsUseCase {

    private final EventRepository eventRepository;
    private final StreamCredentialRepository streamCredentialRepository;

    public GetCredentialsService(EventRepository eventRepository, StreamCredentialRepository streamCredentialRepository) {
        this.eventRepository = eventRepository;
        this.streamCredentialRepository = streamCredentialRepository;
    }

    @Override
    public StreamCredential getCredentials(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        if (event.getStatus() != EventStatus.READY && event.getStatus() != EventStatus.LIVE) {
            throw new EventNotReadyException("Event is still being provisioned or failed");
        }

        return streamCredentialRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("Credentials not found for ready event"));
    }
}
