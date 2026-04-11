package com.enlace.infrastructure.aws;

import com.enlace.domain.model.Plan;
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

    @Value("${aws.ivs.recording-configuration-arn}")
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
    public IvsChannelResult createChannel(String eventSlug, com.enlace.domain.model.Plan plan) {
        ChannelLatencyMode latencyMode = plan == Plan.PREMIUM
                ? ChannelLatencyMode.LOW
                : ChannelLatencyMode.NORMAL;

        log.info("Chamando AWS IVS para criar canal: {} (plano={} latencia={})", eventSlug, plan, latencyMode);
        CreateChannelRequest request = CreateChannelRequest.builder()
                .name(eventSlug)
                .type(ChannelType.STANDARD)
                .latencyMode(latencyMode)
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

        // Verifica se o canal está transmitindo antes de deletar
        try {
            GetStreamResponse stream = ivsClient.getStream(
                    GetStreamRequest.builder().channelArn(channelArn).build()
            );
            if (stream.stream().state() == StreamState.LIVE) {
                throw new IllegalStateException("Cannot delete a live channel");
            }
        } catch (StreamUnavailableException | ChannelNotBroadcastingException e) {
            // Stream não ativo ou canal não está transmitindo - pode deletar
            log.debug("Stream não ativo para o canal {}, prosseguindo com a deleção", channelArn);
        }

        // Deleta a stream key primeiro (se existir)
        if (streamKeyArn != null) {
            try {
                ivsClient.deleteStreamKey(DeleteStreamKeyRequest.builder()
                        .arn(streamKeyArn)
                        .build());
                log.debug("Stream key deletada: {}", streamKeyArn);
            } catch (Exception e) {
                log.warn("Erro ao deletar stream key {}: {}", streamKeyArn, e.getMessage());
                // Continua mesmo se falhar - a stream key pode já ter sido deletada
            }
        }

        // Deleta o canal
        try {
            ivsClient.deleteChannel(DeleteChannelRequest.builder()
                    .arn(channelArn)
                    .build());
            log.info("Canal IVS deletado com sucesso na AWS: {}", channelArn);
        } catch (ChannelNotBroadcastingException e) {
            // Ignora se o canal não está transmitindo - não é um erro para deleção
            log.debug("Canal não estava transmitindo durante deleção: {}", channelArn);
        }
    }

    @Override
    public Optional<RecordingResult> findRecording(String recordingS3KeyPrefix) {
        // O evento "Recording End" do EventBridge já traz o prefixo exato da gravação no S3.
        // Formato: ivs/v1/{accountId}/{channelId}/{ano}/{mes}/{dia}/{hora}/{min}/{recordingId}
        String key = recordingS3KeyPrefix + "/events/recording-ended.json";

        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(recordingBucket)
                            .key(key)
                            .build()
            );

            JsonNode root = objectMapper.readTree(response.asUtf8String());
            JsonNode hls = root.path("media").path("hls");

            String hlsPath    = hls.path("path").asText();          // "media/hls"
            String masterFile = hls.path("playlist").asText();       // "master.m3u8"
            long durationMs   = hls.path("duration_ms").asLong();

            String masterKey = recordingS3KeyPrefix + "/" + hlsPath + "/" + masterFile;

            List<IvsGateway.RenditionInfo> renditions = new ArrayList<>();
            hls.path("renditions").forEach(r -> {
                String quality      = r.path("path").asText();       // "160p30"
                String playlistFile = r.path("playlist").asText();   // "playlist.m3u8"
                String s3Key = recordingS3KeyPrefix + "/" + hlsPath + "/" + quality + "/" + playlistFile;
                renditions.add(new IvsGateway.RenditionInfo(quality, s3Key));
            });

            return Optional.of(new RecordingResult(masterKey, durationMs, renditions));

        } catch (NoSuchKeyException e) {
            log.warn("recording-ended.json não encontrado no S3: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao buscar gravação (prefixo={}): {}", recordingS3KeyPrefix, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<S3ObjectInfo> listObjects(String prefix) {
        log.info("Listando objetos no S3 para o bucket: {} e prefixo: {}", recordingBucket, prefix);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(recordingBucket)
                .prefix(prefix)
                .build();

        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            
            return response.contents().stream()
                    .map(obj -> new S3ObjectInfo(obj.key(), obj.size(), obj.lastModified()))
                    .collect(Collectors.toList());
        } catch (NoSuchBucketException e) {
            log.error("Bucket {} não encontrado: {}", recordingBucket, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro ao listar objetos no S3: {}", e.getMessage(), e);
            throw e;
        }
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
