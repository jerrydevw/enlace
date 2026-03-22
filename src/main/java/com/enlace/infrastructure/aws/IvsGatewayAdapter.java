package com.enlace.infrastructure.aws;

import com.enlace.domain.port.out.IvsGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.ivs.model.*;

@Component
public class IvsGatewayAdapter implements IvsGateway {

    private final IvsClient ivsClient;

    @Value("${aws.ivs.recording-role-arn}")
    private String recordingRoleArn;

    @Value("${aws.ivs.recording-bucket}")
    private String recordingBucket;

    public IvsGatewayAdapter(IvsClient ivsClient) {
        this.ivsClient = ivsClient;
    }

    @Override
    public IvsChannelResult createChannel(String eventSlug) {
        CreateChannelRequest request = CreateChannelRequest.builder()
                .name(eventSlug)
                .type(ChannelType.STANDARD)
                .latencyMode(ChannelLatencyMode.LOW)
                .build();

        CreateChannelResponse response = ivsClient.createChannel(request);

        return new IvsChannelResult(
                response.channel().arn(),
                response.streamKey().arn(),
                response.channel().ingestEndpoint(),
                response.streamKey().value(),
                response.channel().playbackUrl()
        );
    }

    @Override
    public void deleteChannel(String channelArn, String streamKeyArn) {
        if (streamKeyArn != null) {
            try {
                ivsClient.deleteStreamKey(DeleteStreamKeyRequest.builder()
                        .arn(streamKeyArn)
                        .build());
            } catch (Exception e) {
                // Log and continue
            }
        }
        
        ivsClient.deleteChannel(DeleteChannelRequest.builder()
                .arn(channelArn)
                .build());
    }

    @Override
    public void configureRecording(String channelArn, String s3Prefix) {
        // In a real scenario, we might create a RecordingConfiguration first
        // and then associate it with the channel.
        // For simplicity in Sprint 1, we assume a recording configuration might be pre-created
        // or we create a basic one here.

        CreateRecordingConfigurationRequest recordingRequest = CreateRecordingConfigurationRequest.builder()
                .name("recording-config-" + channelArn.substring(channelArn.lastIndexOf("/") + 1))
                .destinationConfiguration(DestinationConfiguration.builder()
                        .s3(S3DestinationConfiguration.builder()
                                .bucketName(recordingBucket)
                                .build())
                        .build())
                .recordingReconnectWindowSeconds(60)
                .build();

        CreateRecordingConfigurationResponse recordingResponse = ivsClient.createRecordingConfiguration(recordingRequest);

        UpdateChannelRequest updateRequest = UpdateChannelRequest.builder()
                .arn(channelArn)
                .recordingConfigurationArn(recordingResponse.recordingConfiguration().arn())
                .build();

        ivsClient.updateChannel(updateRequest);
    }
}
