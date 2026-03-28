package com.enlace.domain.exception;

public class MessagePublishingException extends RuntimeException {
    public MessagePublishingException(String message, Exception cause) {
        super(message, cause);
    }
}
