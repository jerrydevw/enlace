package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.Customer;
import com.enlace.domain.model.Plan;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CustomerEntity() {}

    public static CustomerEntity fromDomain(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.getId();
        entity.name = customer.getName();
        entity.email = customer.getEmail();
        entity.plan = customer.getPlan();
        entity.createdAt = customer.getCreatedAt();
        return entity;
    }

    public Customer toDomain() {
        return new Customer(id, name, email, plan, createdAt);
    }
}
