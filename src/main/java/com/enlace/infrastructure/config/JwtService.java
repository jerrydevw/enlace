package com.enlace.infrastructure.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final String secret;
    private final int expirationHours;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours}") int expirationHours) {
        this.secret = secret;
        this.expirationHours = expirationHours;
    }

    public String generateToken(UUID viewerTokenId, UUID eventId, String eventSlug, String jti) {
        try {
            JWSSigner signer = new MACSigner(secret);
            Instant now = Instant.now();
            Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("viewer:" + viewerTokenId)
                    .jwtID(jti)
                    .claim("event_id", eventId.toString())
                    .claim("event_slug", eventSlug)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }

    public JWTClaimsSet validateAndParse(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(secret);

            if (!signedJWT.verify(verifier)) {
                throw new JwtException("Invalid signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new JwtException("Token expired");
            }

            return claims;
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Invalid token", e);
        }
    }
}
