package com.enlace.infrastructure.config;

import com.enlace.domain.model.Customer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

@Service
public class CustomerJwtService {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.jwt.expiration-hours}")
    private long expirationHours;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    public CustomerJwtService(java.security.PublicKey publicKey, java.security.PrivateKey privateKey) {
        this.publicKey = (RSAPublicKey) publicKey;
        this.privateKey = (RSAPrivateKey) privateKey;

        // Encoder e Decoder construídos uma única vez com as chaves já carregadas
        RSAKey rsaKey = new RSAKey.Builder(this.publicKey)
                .privateKey(this.privateKey)
                .build();

        this.jwtEncoder = new NimbusJwtEncoder((jwkSelector, context) ->
                new JWKSet(rsaKey).getKeys()
        );

        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

    public String generateToken(Customer customer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("enlace")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationHours * 3600))
                .subject(customer.getId().toString())
                .claim("type", "USER")
                .claim("email", customer.getEmail())
                .claim("plan", customer.getPlan().name())
                .claim("roles", "ROLE_CUSTOMER")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(Customer customer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("enlace")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshExpirationDays * 24 * 3600))
                .subject(customer.getId().toString())
                .claim("type", "REFRESH")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}