package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.CreateEventUseCase;
import com.enlace.domain.port.in.DeleteEventUseCase;
import com.enlace.domain.port.in.GetCredentialsUseCase;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.infrastructure.web.dto.CreateEventRequest;
import com.enlace.infrastructure.web.dto.CredentialsResponse;
import com.enlace.infrastructure.web.dto.EventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final GetCredentialsUseCase getCredentialsUseCase;

    public EventController(CreateEventUseCase createEventUseCase, GetEventUseCase getEventUseCase, 
                           DeleteEventUseCase deleteEventUseCase, GetCredentialsUseCase getCredentialsUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.getCredentialsUseCase = getCredentialsUseCase;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        Event event = createEventUseCase.create(new CreateEventUseCase.CreateEventCommand(
                request.customerId(),
                request.title(),
                request.scheduledAt()
        ));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        Event event = getEventUseCase.getById(id);
        return ResponseEntity.ok(toResponse(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteEventUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/credentials")
    public ResponseEntity<CredentialsResponse> getCredentials(@PathVariable UUID id) {
        StreamCredential credential = getCredentialsUseCase.getCredentials(id);
        return ResponseEntity.ok(new CredentialsResponse(
                credential.getRtmpEndpoint(),
                credential.getStreamKey(),
                credential.getExpiresAt()
        ));
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getScheduledAt(),
                event.getStatus(),
                event.getCreatedAt()
        );
    }
}
