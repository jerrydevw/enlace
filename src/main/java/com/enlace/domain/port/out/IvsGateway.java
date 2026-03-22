package com.enlace.domain.port.out;

public interface IvsGateway {
    IvsChannelResult createChannel(String eventSlug);
    void deleteChannel(String channelArn, String streamKeyArn);
    void configureRecording(String channelArn, String s3Prefix);

    record IvsChannelResult(
        String channelArn,
        String streamKeyArn,
        String ingestEndpoint,
        String streamKey,
        String playbackUrl
    ) {}
}
