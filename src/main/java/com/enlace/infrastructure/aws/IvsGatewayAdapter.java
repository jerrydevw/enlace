package com.enlace.infrastructure.aws;

import com.enlace.domain.port.out.IvsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.ivs.model.*;

@Slf4j
@Component
public class IvsGatewayAdapter implements IvsGateway {

    private final IvsClient ivsClient;

    @Value("${aws.ivs.recording-role-arn}")
    private String recordingRoleArn;

    @Value("${aws.ivs.recording-bucket}")
    private String recordingBucket;

    @Value("${aws.ivs.recording-configuration-arn:}")
    private String recordingConfigurationArn;

    public IvsGatewayAdapter(IvsClient ivsClient) {
        this.ivsClient = ivsClient;
    }

    @Override
    public IvsChannelResult createChannel(String eventSlug) {
        log.info("Chamando AWS IVS para criar canal: {}", eventSlug);
        CreateChannelRequest request = CreateChannelRequest.builder()
                .name(eventSlug)
                .type(ChannelType.STANDARD)
                .latencyMode(ChannelLatencyMode.LOW)
                .build();

        CreateChannelResponse response = ivsClient.createChannel(request);
        log.info("Canal IVS criado com sucesso na AWS: {}", response.channel().arn());

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
        log.info("Chamando AWS IVS para deletar canal: {}", channelArn);
        if (streamKeyArn != null) {
            try {
                ivsClient.deleteStreamKey(DeleteStreamKeyRequest.builder()
                        .arn(streamKeyArn)
                        .build());
            } catch (Exception e) {
                log.warn("Erro ao deletar stream key {}: {}", streamKeyArn, e.getMessage());
                // Log and continue
            }
        }
        
        ivsClient.deleteChannel(DeleteChannelRequest.builder()
                .arn(channelArn)
                .build());
        log.info("Canal IVS deletado com sucesso na AWS: {}", channelArn);
    }

    @Override
    public void configureRecording(String channelArn, String s3Prefix) {
        log.info("Associando recording configuration — arn: '{}', channelArn: '{}'",
                recordingConfigurationArn, channelArn);

        UpdateChannelRequest updateRequest = UpdateChannelRequest.builder()
                .arn(channelArn)
                .recordingConfigurationArn(recordingConfigurationArn)
                .build();

        ivsClient.updateChannel(updateRequest);
    }
}
