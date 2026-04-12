package com.enlace.domain.port.out;

import java.util.Optional;
import java.util.List;

public interface IvsGateway {
    IvsChannelResult createChannel(String eventSlug, com.enlace.domain.model.Plan plan);
    void deleteChannel(String channelArn, String streamKeyArn);
    void stopStream(String channelArn);
    Optional<RecordingResult> findRecording(String recordingS3KeyPrefix);

    record IvsChannelResult(
        String channelArn,
        String streamKeyArn,
        String ingestEndpoint,
        String streamKey,
        String playbackUrl
    ) {}

    record RenditionInfo(
        String quality,       // ex: "160p30"
        String s3Key          // chave completa no S3
    ) {}

    record RecordingResult(
        String masterPlaylistKey,
        long durationMs,
        List<RenditionInfo> renditions
    ) {}

    record S3ObjectInfo(
        String key,
        long sizeBytes,
        java.time.Instant lastModified
    ) {}

    List<S3ObjectInfo> listObjects(String prefix);

    String generatePresignedUrl(String key, long expirationMinutes);
}
