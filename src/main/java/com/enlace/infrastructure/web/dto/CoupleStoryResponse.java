package com.enlace.infrastructure.web.dto;

import java.util.List;

public record CoupleStoryResponse(
    String partner1Name,
    String partner2Name,
    String message,
    List<Integer> photos
) {}
