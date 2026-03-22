package com.enlace.domain.port.out;

import com.enlace.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Optional<Customer> findById(UUID id);
}
