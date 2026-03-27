package com.enlace.domain.exception;

public class InviteCodeAlreadyUsedException extends RuntimeException {
    public InviteCodeAlreadyUsedException(String message) {
        super(message);
    }
}
