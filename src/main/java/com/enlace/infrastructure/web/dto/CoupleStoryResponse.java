package com.enlace.infrastructure.web.dto;

public record CoupleStoryResponse(
    String partner1Name,
    String partner2Name,
    String message
) {}
