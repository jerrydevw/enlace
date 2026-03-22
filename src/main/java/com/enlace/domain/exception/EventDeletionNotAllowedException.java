package com.enlace.domain.exception;

public class EventDeletionNotAllowedException extends RuntimeException {
    public EventDeletionNotAllowedException(String message) {
        super(message);
    }
}
