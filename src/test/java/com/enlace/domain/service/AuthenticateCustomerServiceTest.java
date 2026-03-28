package com.enlace.domain.service;

import com.enlace.domain.exception.EmailAlreadyExistsException;
import com.enlace.domain.exception.InvalidCredentialsException;
import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Plan;
import com.enlace.domain.port.in.AuthenticateCustomerUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.infrastructure.config.CustomerJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateCustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomerJwtService jwtService;

    @InjectMocks
    private AuthenticateCustomerService authenticateCustomerService;

    private AuthenticateCustomerUseCase.RegisterCommand registerCommand;
    private AuthenticateCustomerUseCase.LoginCommand loginCommand;
    private Customer customer;

    @BeforeEach
    void setUp() {
        registerCommand = new AuthenticateCustomerUseCase.RegisterCommand("Test User", "test@example.com", "password123");
        loginCommand = new AuthenticateCustomerUseCase.LoginCommand("test@example.com", "password123");
        customer = new Customer(UUID.randomUUID(), "Test User", "test@example.com", Plan.BASIC, "encodedPassword", Instant.now(), null);
    }

    @Test
    void register_ShouldSaveCustomer_WhenEmailIsUnique() {
        when(customerRepository.findByEmail(registerCommand.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerCommand.password())).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = authenticateCustomerService.register(registerCommand);

        assertNotNull(result);
        assertEquals(customer.getEmail(), result.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        when(customerRepository.findByEmail(registerCommand.email())).thenReturn(Optional.of(customer));

        assertThrows(EmailAlreadyExistsException.class, () -> authenticateCustomerService.register(registerCommand));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void login_ShouldReturnResult_WhenCredentialsAreValid() {
        when(customerRepository.findByEmail(loginCommand.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(loginCommand.password(), customer.getPassword())).thenReturn(true);
        when(jwtService.generateToken(customer)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(customer)).thenReturn("refreshToken");

        AuthenticateCustomerUseCase.LoginResult result = authenticateCustomerService.login(loginCommand);

        assertNotNull(result);
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());
        assertEquals(customer.getId(), result.customer().getId());
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        when(customerRepository.findByEmail(loginCommand.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticateCustomerService.login(loginCommand));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(customerRepository.findByEmail(loginCommand.email())).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(loginCommand.password(), customer.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authenticateCustomerService.login(loginCommand));
    }
}
