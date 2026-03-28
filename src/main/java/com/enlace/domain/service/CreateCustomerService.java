package com.enlace.domain.service;

import com.enlace.domain.model.Customer;
import com.enlace.domain.port.in.CreateCustomerUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateCustomerService implements CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public CreateCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerCommand command) {
        Customer customer = new Customer(
            UUID.randomUUID(),
            command.name(),
            command.email(),
            command.plan(),
            "CHANGE_ME",
            Instant.now(),
            null
        );
        return customerRepository.save(customer);
    }
}
