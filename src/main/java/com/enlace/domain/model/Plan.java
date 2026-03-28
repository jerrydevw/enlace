package com.enlace.domain.model;

import lombok.Getter;

@Getter
public enum Plan {
    BASIC(1, 50),
    PRO(10, 500);

    private final int maxActiveEvents;
    private final int maxTokensPerEvent;

    Plan(int maxActiveEvents, int maxTokensPerEvent) {
        this.maxActiveEvents = maxActiveEvents;
        this.maxTokensPerEvent = maxTokensPerEvent;
    }
}
