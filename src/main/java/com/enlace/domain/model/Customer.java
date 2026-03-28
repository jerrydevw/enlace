package com.enlace.domain.model;
 
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.Instant;
import java.util.UUID;
 
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    private UUID id;
    private String name;
    private String email;
    private Plan plan;
    private String password;
    private Instant createdAt;
    private Instant deletedAt;

    public Customer(UUID id, String name, String email, Plan plan, String password, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.plan = plan;
        this.password = password;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
}
