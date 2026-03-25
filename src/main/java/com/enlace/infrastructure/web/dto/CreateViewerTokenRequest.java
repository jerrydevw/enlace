package com.enlace.infrastructure.web.dto;

import java.util.List;

public record CreateViewerTokenRequest(
    String label,
    Integer count
) {}
