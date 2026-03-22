package com.enlace.infrastructure.web.advice;

import com.enlace.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class, EventNotFoundException.class, ViewerTokenNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, getErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler({EventDeletionNotAllowedException.class, EventNotReadyException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, getErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Invalid request arguments");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleFallback(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        ErrorResponse body = new ErrorResponse(status.value(), error, message, Instant.now());
        return ResponseEntity.status(status).body(body);
    }

    private String getErrorCode(RuntimeException ex) {
        if (ex instanceof CustomerNotFoundException) return "CUSTOMER_NOT_FOUND";
        if (ex instanceof EventNotFoundException) return "EVENT_NOT_FOUND";
        if (ex instanceof ViewerTokenNotFoundException) return "VIEWER_TOKEN_NOT_FOUND";
        if (ex instanceof EventDeletionNotAllowedException) return "EVENT_DELETION_NOT_ALLOWED";
        if (ex instanceof EventNotReadyException) return "EVENT_NOT_READY";
        return "ILLEGAL_STATE";
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {}
}
