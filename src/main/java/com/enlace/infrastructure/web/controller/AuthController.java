package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Customer;
import com.enlace.domain.port.in.AuthenticateCustomerUseCase;
import com.enlace.domain.port.in.RefreshTokenUseCase;
import com.enlace.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para registro e login de customers.")
public class AuthController {

    private final AuthenticateCustomerUseCase authenticateUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo customer", description = "Cria uma conta para o customer gerenciar seus eventos.")
    public ResponseEntity<LoginResponse.CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        Customer customer = authenticateUseCase.register(new AuthenticateCustomerUseCase.RegisterCommand(
                request.name(),
                request.email(),
                request.password()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toCustomerResponse(customer));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar customer", description = "Valida credenciais e retorna tokens JWT de acesso e refresh.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticateCustomerUseCase.LoginResult result = authenticateUseCase.login(new AuthenticateCustomerUseCase.LoginCommand(
                request.email(),
                request.password()
        ));
        
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                toCustomerResponse(result.customer())
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acesso", description = "Gera um novo token de acesso a partir de um refresh token válido.")
    public ResponseEntity<RefreshTokenUseCase.RefreshResult> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenUseCase.RefreshResult result = refreshTokenUseCase.refresh(new RefreshTokenUseCase.RefreshCommand(
                request.refreshToken()
        ));
        return ResponseEntity.ok(result);
    }

    private LoginResponse.CustomerResponse toCustomerResponse(Customer customer) {
        return new LoginResponse.CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPlan().name(),
                customer.getCreatedAt()
        );
    }
}
