package com.enlace.domain.exception;

public class CustomerNotValidatedException extends RuntimeException {
    public CustomerNotValidatedException(String message) {
        super(message);
    }
}
