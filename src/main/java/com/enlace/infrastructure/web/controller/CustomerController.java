package com.enlace.infrastructure.web.controller;

import com.enlace.domain.model.Customer;
import com.enlace.domain.port.in.CreateCustomerUseCase;
import com.enlace.infrastructure.web.dto.CreateCustomerRequest;
import com.enlace.infrastructure.web.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Recebendo requisição para cadastrar customer: {}", request.email());
        Customer customer = createCustomerUseCase.create(new CreateCustomerUseCase.CreateCustomerCommand(
            request.name(),
            request.email(),
            request.plan()
        ));
        log.info("Customer cadastrado com sucesso: ID={}", customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getPlan(),
            customer.getCreatedAt()
        ));
    }
}
