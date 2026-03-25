package com.enlace.domain.exception;

public class SessionRevokedException extends RuntimeException {
    public SessionRevokedException(String message) {
        super(message);
    }
}
