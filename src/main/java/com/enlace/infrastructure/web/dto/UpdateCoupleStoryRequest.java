package com.enlace.infrastructure.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateCoupleStoryRequest(
    @Size(max = 100) String partner1Name,
    @Size(max = 100) String partner2Name,
    String message
) {}
