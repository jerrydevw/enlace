package com.enlace.domain.exception;

public class ViewerTokenNotFoundException extends RuntimeException {
    public ViewerTokenNotFoundException(String message) {
        super(message);
    }
}
