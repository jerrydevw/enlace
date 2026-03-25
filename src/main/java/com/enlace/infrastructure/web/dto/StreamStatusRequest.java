package com.enlace.infrastructure.web.dto;

public record StreamStatusRequest(
        String channelName,
        String eventName,
        String streamId
) {}
