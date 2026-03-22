package com.enlace.infrastructure.web.dto;

import java.util.List;

public record CreateViewerTokenRequest(
    List<String> labels
) {}
