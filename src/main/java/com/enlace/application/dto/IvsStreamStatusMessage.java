package com.enlace.application.dto;

public record IvsStreamStatusMessage(
    String channelName,
    String eventName,
    String streamId,
    String recordingS3KeyPrefix  // presente apenas no evento "Recording End"
) {}
