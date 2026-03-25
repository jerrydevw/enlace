package com.enlace.domain.exception;

public class EventNotLiveException extends RuntimeException {
    public EventNotLiveException(String message) {
        super(message);
    }
}
