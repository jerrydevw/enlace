package com.enlace.domain.model;

import lombok.Getter;

import java.util.List;

@Getter
public enum Plan {
    BASIC("Plano Básico", 10, 5, 29.90, "BRL"),
    PREMIUM("Plano Premium", 50, 20, 69.90, "BRL");

    private final String displayName;
    private final int maxViewersPerEvent;
    private final int recordingRetentionDays;
    private final double pricePerEvent;
    private final String currency;

    Plan(String displayName, int maxViewersPerEvent, int recordingRetentionDays,
         double pricePerEvent, String currency) {
        this.displayName = displayName;
        this.maxViewersPerEvent = maxViewersPerEvent;
        this.recordingRetentionDays = recordingRetentionDays;
        this.pricePerEvent = pricePerEvent;
        this.currency = currency;
    }

    public List<String> getFeatures() {
        return List.of(
                "Até " + maxViewersPerEvent + " espectadores simultâneos",
                "Gravação disponível por " + recordingRetentionDays + " dias",
                "R$ " + String.format("%.2f", pricePerEvent) + " por evento"
        );
    }
}