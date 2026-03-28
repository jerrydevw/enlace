package com.enlace.application.dto;

public record IvsStreamStatusMessage(
    String channelName,
    String eventName,
    String streamId
) {}
