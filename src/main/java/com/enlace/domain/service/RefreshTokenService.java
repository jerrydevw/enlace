package com.enlace.domain.service;

import com.enlace.domain.model.Customer;
import com.enlace.domain.port.in.RefreshTokenUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.infrastructure.config.CustomerJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final CustomerJwtService jwtService;
    private final CustomerRepository customerRepository;

    @Override
    public RefreshResult refresh(RefreshCommand command) {
        Jwt decodedToken = jwtService.decode(command.refreshToken());
        
        String type = decodedToken.getClaim("type");
        if (!"REFRESH".equals(type)) {
            throw new IllegalArgumentException("Token inválido");
        }

        UUID customerId = UUID.fromString(decodedToken.getSubject());
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer não encontrado"));

        String newAccessToken = jwtService.generateToken(customer);
        return new RefreshResult(newAccessToken);
    }
}
