package com.enlace.infrastructure.web.advice;

import com.enlace.domain.exception.*;
import com.enlace.domain.exception.SessionConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class, EventNotFoundException.class, ViewerTokenNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, getErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler({EventDeletionNotAllowedException.class, EventNotReadyException.class, EventNotLiveException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        log.warn("Conflito de estado: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, getErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler({InvalidInviteCodeException.class, InviteCodeAlreadyUsedException.class, SessionRevokedException.class, JwtException.class, InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
        log.warn("Não autorizado: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, getErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email já existe: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(EventEndedException.class)
    public ResponseEntity<ErrorResponse> handleGone(EventEndedException ex) {
        log.warn("Recurso não disponível (Gone): {}", ex.getMessage());
        return buildResponse(HttpStatus.GONE, "EVENT_ENDED", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Acesso proibido: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleUnprocessable(PlanLimitExceededException ex) {
        log.warn("Limite de plano excedido: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "PLAN_LIMIT_EXCEEDED", ex.getMessage());
    }

    @ExceptionHandler(SessionConflictException.class)
    public ResponseEntity<ErrorResponse> handleSessionConflict(SessionConflictException ex) {
        log.warn("Conflito de sessão: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "SESSION_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(RateLimitExceededException ex) {
        log.warn("Rate limit excedido: {}", ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage());
    }

    @ExceptionHandler(MessagePublishingException.class)
    public ResponseEntity<ErrorResponse> handleMessagePublishing(MessagePublishingException ex) {
        log.error("Falha ao publicar mensagem: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "MESSAGE_PUBLISHING_FAILED", "Failed to publish message to queue");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Erro de validação: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Invalid request arguments");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleFallback(Exception ex) {
        if (ex.getClass().getName().equals("software.amazon.awssdk.services.s3.model.NoSuchBucketException")) {
            log.error("Bucket S3 não encontrado: {}", ex.getMessage());
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "S3_BUCKET_NOT_FOUND", "Configured S3 bucket does not exist");
        }
        log.error("Erro inesperado capturado: {}", ex.getMessage(), ex);
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
        if (ex instanceof EventNotLiveException) return "EVENT_NOT_LIVE";
        if (ex instanceof InvalidInviteCodeException) return "INVALID_INVITE_CODE";
        if (ex instanceof InviteCodeAlreadyUsedException) return "INVITE_CODE_ALREADY_USED";
        if (ex instanceof SessionRevokedException) return "SESSION_REVOKED";
        if (ex instanceof InvalidCredentialsException) return "INVALID_CREDENTIALS";
        if (ex instanceof JwtException) return "INVALID_TOKEN";
        return "ILLEGAL_STATE";
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {}
}
