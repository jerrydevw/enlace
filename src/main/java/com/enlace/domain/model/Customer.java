package com.enlace.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Customer {
    private UUID id;
    private String name;
    private String email;
    private Plan plan;
    private Instant createdAt;

    public Customer() {}

    public Customer(UUID id, String name, String email, Plan plan, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.plan = plan;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
