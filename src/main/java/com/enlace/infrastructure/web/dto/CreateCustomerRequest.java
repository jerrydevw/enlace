package com.enlace.infrastructure.web.dto;

import com.enlace.domain.model.Plan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotNull Plan plan
) {}
