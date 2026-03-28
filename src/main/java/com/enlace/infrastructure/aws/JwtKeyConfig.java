package com.enlace.infrastructure.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    private final SecretsManagerClient secretsManagerClient;

    @Value("${aws.secrets.jwt-private-key-arn}")
    private String privateKeyArn;

    @Value("${aws.secrets.jwt-public-key-arn}")
    private String publicKeyArn;

    @Value("${aws.secrets.jwt-secret-arn}")
    private String jwtSecretArn;

    public JwtKeyConfig(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    @Bean
    public PrivateKey privateKey() throws Exception {
        String pem = getSecret(privateKeyArn);
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(cleaned);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        String pem = getSecret(publicKeyArn);
        String cleaned = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(cleaned);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    @Bean
    public String jwtSecret() {
        return getSecret(jwtSecretArn);
    }

    private String getSecret(String secretArn) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretArn)
                .build();

        return secretsManagerClient.getSecretValue(request).secretString();
    }
}