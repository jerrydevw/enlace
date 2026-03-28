package com.enlace.domain.service;

import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Plan;
import com.enlace.domain.port.in.AuthenticateCustomerUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.infrastructure.config.CustomerJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticateCustomerService implements AuthenticateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerJwtService jwtService;

    @Override
    public Customer register(RegisterCommand command) {
        if (customerRepository.findByEmail(command.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Customer customer = new Customer(
                UUID.randomUUID(),
                command.name(),
                command.email(),
                Plan.BASIC,
                passwordEncoder.encode(command.password()),
                Instant.now(),
                null
        );

        return customerRepository.save(customer);
    }

    @Override
    public LoginResult login(LoginCommand command) {
        Customer customer = customerRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!passwordEncoder.matches(command.password(), customer.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String accessToken = jwtService.generateToken(customer);
        String refreshToken = jwtService.generateRefreshToken(customer);

        return new LoginResult(accessToken, refreshToken, customer);
    }
}
