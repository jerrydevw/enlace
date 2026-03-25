package com.enlace.infrastructure.config;

import com.enlace.domain.model.ViewerSession;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.UUID;

public class ViewerAuthentication extends AbstractAuthenticationToken {

    private final JWTClaimsSet claims;
    private final ViewerSession session;

    public ViewerAuthentication(JWTClaimsSet claims, ViewerSession session) {
        super(java.util.Collections.emptyList());
        this.claims = claims;
        this.session = session;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return session.getViewerToken().getId();
    }

    public String getJti() {
        return session.getJti();
    }

    public UUID getEventId() {
        return session.getEvent().getId();
    }
}
