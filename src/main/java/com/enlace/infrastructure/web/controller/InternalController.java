package com.enlace.infrastructure.web.controller;

import com.enlace.domain.port.in.UpdateStreamStatusUseCase;
import com.enlace.infrastructure.web.dto.StreamStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    private static final Logger log = LoggerFactory.getLogger(InternalController.class);

    private final UpdateStreamStatusUseCase updateStreamStatusUseCase;

    @Value("${enlace.internal-api-key}")
    private String internalApiKey;

    public InternalController(UpdateStreamStatusUseCase updateStreamStatusUseCase) {
        this.updateStreamStatusUseCase = updateStreamStatusUseCase;
    }

    @PostMapping("/events/stream-status")
    public ResponseEntity<Void> updateStreamStatus(
            @RequestHeader("X-Internal-Key") String key,
            @RequestBody StreamStatusRequest request) {

        if (!key.equals(internalApiKey)) {
            log.warn("Tentativa de acesso interno com chave invalida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Evento IVS recebido — channel: '{}', event: '{}', stream: '{}'",
                request.channelName(), request.eventName(), request.streamId());

        updateStreamStatusUseCase.update(request.channelName(), request.eventName(), request.streamId());

        return ResponseEntity.ok().build();
    }
}
