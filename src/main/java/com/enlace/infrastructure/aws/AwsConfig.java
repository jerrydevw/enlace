package com.enlace.infrastructure.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AwsConfig {

    @Bean
    @ConfigurationProperties(prefix = "aws")
    public AwsProperties awsProperties() {
        return new AwsProperties();
    }

    @Bean
    public IvsClient ivsClient(AwsProperties props) {
        return IvsClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Client s3Client(AwsProperties props) {
        return S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsProperties props) {
        return S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SecretsManagerClient secretsManagerClient(AwsProperties props) {
        return SecretsManagerClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(AwsProperties props) {
        String region = props.getSqs() != null && props.getSqs().getRegion() != null 
                ? props.getSqs().getRegion() 
                : props.getRegion();
                
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public static class AwsProperties {
        private String region;
        private String accessKey;
        private String secretKey;
        private String endpoint;
        private SqsProperties sqs;

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public SqsProperties getSqs() { return sqs; }
        public void setSqs(SqsProperties sqs) { this.sqs = sqs; }
    }

    public static class SqsProperties {
        private String region;
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }
}