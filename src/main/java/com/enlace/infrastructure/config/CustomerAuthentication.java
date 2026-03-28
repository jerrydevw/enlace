package com.enlace.infrastructure.config;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomerAuthentication extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final UUID customerId;

    public CustomerAuthentication(Jwt jwt) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        this.jwt = jwt;
        this.customerId = UUID.fromString(jwt.getSubject());
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public String getName() {
        return customerId.toString();
    }
}
