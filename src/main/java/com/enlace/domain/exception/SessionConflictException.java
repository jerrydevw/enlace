package com.enlace.domain.exception;

public class SessionConflictException extends RuntimeException {
    public SessionConflictException(String message) {
        super(message);
    }
}
