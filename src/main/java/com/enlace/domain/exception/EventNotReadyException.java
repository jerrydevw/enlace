package com.enlace.domain.exception;

public class EventNotReadyException extends RuntimeException {
    public EventNotReadyException(String message) {
        super(message);
    }
}
