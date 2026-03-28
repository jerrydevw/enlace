package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.CreateEventUseCase;
import com.enlace.domain.port.in.DeleteEventUseCase;
import com.enlace.domain.port.in.GetCredentialsUseCase;
import com.enlace.domain.port.in.GetEventUseCase;
import com.enlace.domain.port.in.UpdateEventUseCase;
import com.enlace.domain.port.in.ViewerHeartbeatUseCase;
import com.enlace.domain.service.EventOwnershipValidator;
import com.enlace.infrastructure.config.SecurityUtils;
import com.enlace.infrastructure.web.dto.CreateEventRequest;
import com.enlace.infrastructure.web.dto.UpdateEventRequest;
import com.enlace.infrastructure.web.dto.CredentialsResponse;
import com.enlace.infrastructure.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Endpoints para gestão de eventos de live.")
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final GetEventUseCase getEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final GetCredentialsUseCase getCredentialsUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final EventOwnershipValidator ownershipValidator;
    private final ViewerHeartbeatUseCase heartbeatUseCase;

    public EventController(CreateEventUseCase createEventUseCase, GetEventUseCase getEventUseCase, 
                           DeleteEventUseCase deleteEventUseCase, GetCredentialsUseCase getCredentialsUseCase,
                           UpdateEventUseCase updateEventUseCase, EventOwnershipValidator ownershipValidator,
                           ViewerHeartbeatUseCase heartbeatUseCase) {
        this.createEventUseCase = createEventUseCase;
        this.getEventUseCase = getEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.getCredentialsUseCase = getCredentialsUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.ownershipValidator = ownershipValidator;
        this.heartbeatUseCase = heartbeatUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar novo evento", description = "Cria um novo evento de live para o customer autenticado.")
    @ApiResponse(responseCode = "202", description = "Evento aceito e provisionamento iniciado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "422", description = "Limite de plano excedido ou dados inválidos")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Recebendo requisição para criar evento: {}", request.title());
        Event event = createEventUseCase.create(new CreateEventUseCase.CreateEventCommand(
                customerId,
                request.title(),
                request.scheduledAt(),
                request.plan()
        ));
        log.info("Evento criado com sucesso: ID={}, Slug={}", event.getId(), event.getSlug());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(toResponse(event));
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Retorna os eventos do customer autenticado com suporte a paginação.")
    public ResponseEntity<PagedResponse<EventResponse>> list(
            @PageableDefault(size = 20, sort = "scheduledAt") Pageable pageable) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Listando eventos para o customer: {} - Page: {}, Size: {}", customerId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventsPage = getEventUseCase.listByCustomerId(customerId, pageable);
        
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new PagedResponse<>(
                content,
                eventsPage.getNumber(),
                eventsPage.getSize(),
                eventsPage.getTotalElements(),
                eventsPage.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter evento por ID", description = "Retorna os detalhes de um evento específico.")
    @ApiResponse(responseCode = "403", description = "Acesso negado (não é dono do evento)")
    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    public ResponseEntity<EventResponse> get(@PathVariable UUID id) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Buscando evento por ID: {}", id);
        ownershipValidator.validate(id, customerId);
        Event event = getEventUseCase.getById(id);
        return ResponseEntity.ok(toResponse(event));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento", description = "Atualiza os dados de um evento.")
    public ResponseEntity<EventResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Recebendo requisição para atualizar evento: {}", id);
        ownershipValidator.validate(id, customerId);
        Event event = updateEventUseCase.update(new UpdateEventUseCase.UpdateEventCommand(
                id,
                request.title(),
                request.scheduledAt()
        ));
        log.info("Evento atualizado com sucesso: ID={}, Slug={}", event.getId(), event.getSlug());
        return ResponseEntity.ok(toResponse(event));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar evento", description = "Remove um evento.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Recebendo requisição para deletar evento: {}", id);
        ownershipValidator.validate(id, customerId);
        deleteEventUseCase.delete(id);
        log.info("Evento deletado com sucesso: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/credentials")
    @Operation(summary = "Obter credenciais de stream", description = "Retorna o endpoint RTMP e a chave de transmissão para o software de live (ex: OBS).")
    public ResponseEntity<CredentialsResponse> getCredentials(@PathVariable UUID id) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Buscando credenciais para o evento: {}", id);
        ownershipValidator.validate(id, customerId);
        StreamCredential credential = getCredentialsUseCase.getCredentials(id);
        return ResponseEntity.ok(new CredentialsResponse(
                credential.getRtmpEndpoint(),
                credential.getStreamKey(),
                credential.getExpiresAt(),
                credential.getDeletedAt()
        ));
    }

    @GetMapping("/{id}/ingestion-url")
    @Operation(summary = "Obter URL de ingestão formatada", description = "Retorna a URL completa rtmps://... para facilitar a configuração.")
    public ResponseEntity<IngestionUrlResponse> getIngestionUrl(@PathVariable UUID id) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        log.info("Buscando URL de ingestão para o evento: {}", id);
        ownershipValidator.validate(id, customerId);
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

    @PostMapping("/{slug}/heartbeat")
    @Operation(summary = "Registrar heartbeat do viewer", description = "Endpoint para o viewer enviar ping a cada 30s.")
    public ResponseEntity<Void> heartbeat(@PathVariable String slug) {
        UUID sessionId = SecurityUtils.getCurrentViewerSessionId();
        UUID eventId = SecurityUtils.getCurrentViewer().getEventId();
        heartbeatUseCase.registerHeartbeat(sessionId, eventId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/viewers/count")
    @Operation(summary = "Contar viewers ativos", description = "Retorna a quantidade de convidados assistindo a live no momento.")
    public ResponseEntity<Map<String, Long>> countViewers(@PathVariable UUID id) {
        UUID customerId = SecurityUtils.getCurrentCustomerId();
        ownershipValidator.validate(id, customerId);
        long count = heartbeatUseCase.countActiveViewers(id);
        return ResponseEntity.ok(Map.of("activeViewers", count));
    }

    private EventResponse toResponse(Event event) {
        boolean liveStarted = event.getStatus() == EventStatus.LIVE || event.getStatus() == EventStatus.ENDED;
        Map<String, Object> planLimits = Map.of(
            "maxViewers", event.getPlan().getMaxViewersPerEvent(),
            "recordingRetentionDays", event.getPlan().getRecordingRetentionDays(),
            "price", event.getPlan().getPricePerEvent()
        );
        return new EventResponse(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getScheduledAt(),
                event.getStatus(),
                event.getPlan(),
                planLimits,
                liveStarted ? event.getIvsPlaybackUrl() : null,
                event.getCreatedAt(),
                event.getDeletedAt()
        );
    }
}
