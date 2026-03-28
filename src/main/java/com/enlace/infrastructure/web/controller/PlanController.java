package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Endpoints para consulta de planos disponíveis.")
public class PlanController {

    @GetMapping
    @Operation(summary = "Listar planos disponíveis", description = "Retorna todos os planos e seus respectivos limites.")
    public ResponseEntity<Map<String, List<PlanInfo>>> listPlans() {
        List<PlanInfo> plans = Arrays.stream(Plan.values())
                .map(plan -> new PlanInfo(
                        plan.name(),
                        plan.getDisplayName(),
                        plan.getMaxViewersPerEvent(),
                        plan.getRecordingRetentionDays(),
                        plan.getPricePerEvent(),
                        plan.getCurrency(),
                        plan.getFeatures()  // ✅ Usa getFeatures() do enum
                ))
                .toList();

        return ResponseEntity.ok(Map.of("plans", plans));
    }

    public record PlanInfo(
            String name,
            String displayName,
            int maxViewers,
            int recordingRetentionDays,
            double price,
            String currency,
            List<String> features
    ) {}
}
