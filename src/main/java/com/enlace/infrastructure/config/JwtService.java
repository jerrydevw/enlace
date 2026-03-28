package com.enlace.infrastructure.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final String jwtSecret;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final int expirationHours;
    private final int viewerTokenTtlHours;

    public JwtService(
            @Value("${app.jwt.secret:}") String jwtSecret,
            PrivateKey privateKey,
            PublicKey publicKey,
            @Value("${app.jwt.expiration-hours}") int expirationHours,
            @Value("${app.viewer-token-ttl-hours}") int viewerTokenTtlHours
    ) {
        this.jwtSecret = jwtSecret;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.expirationHours = expirationHours;
        this.viewerTokenTtlHours = viewerTokenTtlHours;
    }

    // ── Cenário 1: Viewer convidado ──────────────────────────────────────────
    // Assina com HMAC (HS256) usando jwtSecret.
    // Usado quando um convidado informa o código e recebe um token para assistir.

    public String generateViewerToken(UUID viewerTokenId, UUID eventId, String eventSlug, String jti) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(viewerTokenTtlHours, ChronoUnit.HOURS);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("viewer:" + viewerTokenId)
                    .jwtID(jti)
                    .claim("type", "viewer")
                    .claim("event_id", eventId.toString())
                    .claim("event_slug", eventSlug)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(jwtSecret));

            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating viewer token", e);
        }
    }

    public JWTClaimsSet validateAndParseViewerToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(jwtSecret))) {
                throw new JwtException("Invalid viewer token signature");
            }

            return validateExpiration(jwt.getJWTClaimsSet());
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Invalid viewer token", e);
        }
    }

    // ── Cenário 2: Usuário cadastrado ────────────────────────────────────────
    // Assina com RSA (RS256) usando privateKey/publicKey.
    // Usado no login de usuários que criam e gerenciam eventos.

    public String generateUserToken(UUID userId, String email, String role) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .jwtID(UUID.randomUUID().toString())
                    .claim("type", "user")
                    .claim("email", email)
                    .claim("role", role)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    claims
            );
            jwt.sign(new RSASSASigner(privateKey));

            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating user token", e);
        }
    }

    public JWTClaimsSet validateAndParseUserToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new RSASSAVerifier((java.security.interfaces.RSAPublicKey) publicKey))) {
                throw new JwtException("Invalid user token signature");
            }

            return validateExpiration(jwt.getJWTClaimsSet());
        } catch (ParseException | JOSEException e) {
            throw new JwtException("Invalid user token", e);
        }
    }

    // ── Roteamento automático por tipo ───────────────────────────────────────
    // Útil nos filtros de segurança onde o tipo do token não é conhecido a priori.

    public JWTClaimsSet validateAndParse(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();

            if (JWSAlgorithm.HS256.equals(algorithm)) {
                return validateAndParseViewerToken(token);
            } else if (JWSAlgorithm.RS256.equals(algorithm)) {
                return validateAndParseUserToken(token);
            }

            throw new JwtException("Unsupported algorithm: " + algorithm);
        } catch (ParseException e) {
            throw new JwtException("Malformed token", e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JWTClaimsSet validateExpiration(JWTClaimsSet claims) {
        if (claims.getExpirationTime().before(new Date())) {
            throw new JwtException("Token expired");
        }
        return claims;
    }
}