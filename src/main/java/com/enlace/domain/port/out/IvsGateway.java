package com.enlace.domain.port.out;

import java.util.Optional;
import java.util.List;

public interface IvsGateway {
    IvsChannelResult createChannel(String eventSlug);
    void deleteChannel(String channelArn, String streamKeyArn);
    Optional<RecordingResult> findRecording(String s3Prefix, String streamId);

    record IvsChannelResult(
        String channelArn,
        String streamKeyArn,
        String ingestEndpoint,
        String streamKey,
        String playbackUrl
    ) {}

    record RecordingResult(
        String masterPlaylistKey,
        long durationMs,
        List<String> availableQualities
    ) {}
}
