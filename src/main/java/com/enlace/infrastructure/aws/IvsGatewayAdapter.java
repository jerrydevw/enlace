package com.enlace.infrastructure.aws;

import com.enlace.domain.port.out.IvsGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.ivs.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IvsGatewayAdapter implements IvsGateway {

    private final IvsClient ivsClient;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ObjectMapper objectMapper;

    @Value("${aws.ivs.recording-role-arn}")
    private String recordingRoleArn;

    @Value("${aws.ivs.recording-bucket}")
    private String recordingBucket;

    @Value("${aws.ivs.recording-configuration-arn:}")
    private String recordingConfigurationArn;

    public IvsGatewayAdapter(IvsClient ivsClient, S3Client s3Client, S3Presigner s3Presigner, ObjectMapper objectMapper) {
        this.ivsClient = ivsClient;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "ivs")
    @Retry(name = "ivs")
    public IvsChannelResult createChannel(String eventSlug) {
        log.info("Chamando AWS IVS para criar canal: {}", eventSlug);
        CreateChannelRequest request = CreateChannelRequest.builder()
                .name(eventSlug)
                .type(ChannelType.STANDARD)
                .latencyMode(ChannelLatencyMode.LOW)
                .recordingConfigurationArn(recordingConfigurationArn)
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

        try {
            GetStreamResponse stream = ivsClient.getStream(
                    GetStreamRequest.builder().channelArn(channelArn).build()
            );
            if (stream.stream().state() == StreamState.LIVE) {
                throw new IllegalStateException("Cannot delete a live channel");
            }
        } catch (StreamUnavailableException e) {
            // Stream nao ativo, pode deletar
            log.debug("Stream não ativo para o canal {}, prosseguindo com a deleção", channelArn);
        }

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
    public Optional<RecordingResult> findRecording(String s3Prefix, String streamId) {
        // O IVS organiza por data/hora, então busca por prefixo + streamId
        // s3Prefix = ivs/v1/{accountId}/{channelId}
        // streamId vem do evento Stream End do EventBridge
        String key = s3Prefix + "/" + streamId + "/events/recording-ended.json";

        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(recordingBucket)
                            .key(key)
                            .build()
            );

            // Parse do JSON
            JsonNode root = objectMapper.readTree(response.asUtf8String());
            String playlist = root.path("media").path("hls").path("playlist").asText();
            long durationMs = root.path("media").path("hls").path("duration_ms").asLong();

            List<String> qualities = new ArrayList<>();
            root.path("media").path("hls").path("renditions").forEach(r ->
                    qualities.add(r.path("path").asText())
            );

            String masterKey = s3Prefix + "/" + streamId + "/media/hls/" + playlist;

            return Optional.of(new RecordingResult(masterKey, durationMs, qualities));

        } catch (NoSuchKeyException e) {
            log.warn("Recording ainda não disponível para stream {}: {}", streamId, key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao buscar gravação no S3 para stream {}: {}", streamId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<S3ObjectInfo> listObjects(String prefix) {
        log.info("Listando objetos no S3 para o prefixo: {}", prefix);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(recordingBucket)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        
        return response.contents().stream()
                .map(obj -> new S3ObjectInfo(obj.key(), obj.size(), obj.lastModified()))
                .collect(Collectors.toList());
    }

    @Override
    public String generatePresignedUrl(String key, long expirationMinutes) {
        log.info("Gerando URL pré-assinada para: {} (exp: {} min)", key, expirationMinutes);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(recordingBucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
