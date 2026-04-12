package com.enlace.infrastructure.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Component
public class S3PhotoAdapter {

    private final S3Client s3Client;

    @Value("${aws.ivs.recording-bucket}")
    private String bucket;

    public S3PhotoAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void upload(String key, byte[] bytes, String contentType) {
        log.info("Fazendo upload de foto para S3: bucket={}, key={}, size={} bytes", bucket, key, bytes.length);
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build(),
            RequestBody.fromBytes(bytes)
        );
        log.info("Upload concluído: {}", key);
    }

    public byte[] download(String key) {
        log.info("Fazendo download de foto do S3: bucket={}, key={}", bucket, key);
        return s3Client.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        ).asByteArray();
    }

    public void delete(String key) {
        log.info("Deletando foto do S3: bucket={}, key={}", bucket, key);
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            );
        } catch (Exception e) {
            log.warn("Erro ao deletar foto do S3 (key={}): {}", key, e.getMessage());
        }
    }
}
