package com.enlace.infrastructure.persistence;
 
import com.enlace.domain.model.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
 
import java.time.Instant;
import java.util.UUID;
 
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class CustomerEntity {
 
    @Id
    private UUID id;
 
    @Column(nullable = false)
    private String name;
 
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
 
    @SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted_at")
    @Column(name = "deleted_at")
    private Instant deletedAt;
 
    public static CustomerEntity fromDomain(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.getId();
        entity.name = customer.getName();
        entity.email = customer.getEmail();
        entity.password = customer.getPassword();
        entity.createdAt = customer.getCreatedAt();
        entity.deletedAt = customer.getDeletedAt();
        return entity;
    }

    public Customer toDomain() {
        return new Customer(id, name, email, password, createdAt, deletedAt);
    }
}
