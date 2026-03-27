package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.CreateEventUseCase;
import com.enlace.domain.port.in.DeleteEventUseCase;
import com.enlace.domain.port.in.GetCredentialsUseCase;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.in.UpdateEventUseCase;
import com.enlace.infrastructure.web.dto.CreateEventRequest;
import com.enlace.infrastructure.web.dto.UpdateEventRequest;
import com.enlace.infrastructure.web.dto.CredentialsResponse;
import com.enlace.infrastructure.web.dto.EventResponse;
import com.enlace.infrastructure.web.dto.IngestionUrlResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final GetCredentialsUseCase getCredentialsUseCase;
    private final UpdateEventUseCase updateEventUseCase;

    public EventController(CreateEventUseCase createEventUseCase, GetEventUseCase getEventUseCase, 
                           DeleteEventUseCase deleteEventUseCase, GetCredentialsUseCase getCredentialsUseCase,
                           UpdateEventUseCase updateEventUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.getCredentialsUseCase = getCredentialsUseCase;
        this.updateEventUseCase = updateEventUseCase;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        log.info("Recebendo requisição para criar evento: {}", request.title());
        Event event = createEventUseCase.create(new CreateEventUseCase.CreateEventCommand(
                request.customerId(),
                request.title(),
                request.scheduledAt()
        ));
        log.info("Evento criado com sucesso: ID={}, Slug={}", event.getId(), event.getSlug());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(event));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        log.info("Buscando evento por ID: {}", id);
        Event event = getEventUseCase.getById(id);
        return ResponseEntity.ok(toResponse(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
        log.info("Recebendo requisição para atualizar evento: {}", id);
        Event event = updateEventUseCase.update(new UpdateEventUseCase.UpdateEventCommand(
                id,
                request.title(),
                request.scheduledAt()
        ));
        log.info("Evento atualizado com sucesso: ID={}, Slug={}", event.getId(), event.getSlug());
        return ResponseEntity.ok(toResponse(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("Recebendo requisição para deletar evento: {}", id);
        deleteEventUseCase.delete(id);
        log.info("Evento deletado com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/credentials")
    public ResponseEntity<CredentialsResponse> getCredentials(@PathVariable UUID id) {
        log.info("Buscando credenciais para o evento: {}", id);
        StreamCredential credential = getCredentialsUseCase.getCredentials(id);
        return ResponseEntity.ok(new CredentialsResponse(
                credential.getRtmpEndpoint(),
                credential.getStreamKey(),
                credential.getExpiresAt(),
                credential.getDeletedAt()
        ));
    }

    @GetMapping("/{id}/ingestion-url")
    public ResponseEntity<IngestionUrlResponse> getIngestionUrl(@PathVariable UUID id) {
        log.info("Buscando URL de ingestão para o evento: {}", id);
        Event event = getEventUseCase.getById(id);
        
        if (event.getStatus() != EventStatus.READY && event.getStatus() != EventStatus.LIVE && event.getStatus() != EventStatus.ENDED) {
            log.warn("Tentativa de buscar URL de ingestão para evento não provisionado: ID={}, Status={}", id, event.getStatus());
            throw new IllegalStateException("O canal ainda não foi provisionado para este evento.");
        }
        
        StreamCredential credential = getCredentialsUseCase.getCredentials(id);
        
        // Amazon IVS RTMPS URL format: rtmps://<endpoint>:443/app/<stream_key>
        // Note: rtmpEndpoint typically looks like "6dc3f567fe06.global-contribute.live-video.net"
        String ingestionUrl = String.format("rtmps://%s:443/app/%s", 
                credential.getRtmpEndpoint(), 
                credential.getStreamKey());
        
        return ResponseEntity.ok(new IngestionUrlResponse(ingestionUrl));
    }

    private EventResponse toResponse(Event event) {
        boolean liveStarted = event.getStatus() == EventStatus.LIVE || event.getStatus() == EventStatus.ENDED;
        return new EventResponse(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getScheduledAt(),
                event.getStatus(),
                liveStarted ? event.getIvsPlaybackUrl() : null,
                event.getCreatedAt(),
                event.getDeletedAt()
        );
    }
}
