package com.enlace.domain.port.in;

import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Plan;

public interface CreateCustomerUseCase {
    Customer create(CreateCustomerCommand command);

    record CreateCustomerCommand(
        String name,
        String email
    ) {}
}
