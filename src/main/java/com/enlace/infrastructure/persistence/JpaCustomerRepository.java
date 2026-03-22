package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.Customer;
import com.enlace.domain.port.out.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerRepository springDataCustomerRepository;

    public JpaCustomerRepository(SpringDataCustomerRepository springDataCustomerRepository) {
        this.springDataCustomerRepository = springDataCustomerRepository;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return springDataCustomerRepository.findById(id).map(CustomerEntity::toDomain);
    }
}
