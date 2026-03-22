package com.enlace.domain.model;

public enum EventStatus {
    CREATED,
    PROVISIONING,
    READY,
    LIVE,
    ENDED,
    PROVISIONING_FAILED;

    public boolean canTransitionTo(EventStatus next) {
        return switch (this) {
            case CREATED -> next == PROVISIONING || next == PROVISIONING_FAILED;
            case PROVISIONING -> next == READY || next == PROVISIONING_FAILED;
            case READY -> next == LIVE || next == ENDED;
            case LIVE -> next == ENDED;
            case ENDED -> false;
            case PROVISIONING_FAILED -> next == PROVISIONING;
        };
    }
}
