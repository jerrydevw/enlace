package com.enlace.infrastructure.web.controller;

import com.enlace.domain.port.in.ListRecordingsUseCase;
import com.enlace.domain.service.EventOwnershipValidator;
import com.enlace.infrastructure.config.CustomerAuthentication;
import com.enlace.infrastructure.web.dto.RecordingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Recordings", description = "Endpoints para gestão de gravações das lives.")
public class RecordingController {

    private final ListRecordingsUseCase listRecordingsUseCase;
    private final EventOwnershipValidator ownershipValidator;

    @GetMapping("/events/{id}/recordings")
    @Operation(summary = "Listar gravações do evento", description = "Retorna uma lista de arquivos de vídeo disponíveis para um evento.")
    public ResponseEntity<List<RecordingResponse>> listRecordings(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomerAuthentication auth) {
        ownershipValidator.validate(id, auth.getCustomerId());
        return ResponseEntity.ok(listRecordingsUseCase.listRecordings(id));
    }

    @GetMapping("/recordings/{recordingId}/download-url")
    @Operation(summary = "Obter URL de download", description = "Gera uma URL pré-assinada válida por 1 hora para baixar a gravação.")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String recordingId) {
        // Nota: recordingId é a key do S3 encodada em Base64
        String downloadUrl = listRecordingsUseCase.getDownloadUrl(recordingId);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }
}
