package com.enlace.infrastructure.config;

import com.enlace.domain.model.ViewerSession;
import com.enlace.domain.port.out.ViewerSessionRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtViewerAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ViewerSessionRepository sessionRepository;

    public JwtViewerAuthFilter(JwtService jwtService, ViewerSessionRepository sessionRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        log.debug("JwtViewerAuthFilter processando requisição: {} {}", request.getMethod(), request.getRequestURI());

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            log.debug("Ignorando autenticação para requisição OPTIONS: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Nenhum token JWT encontrado para: {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            JWTClaimsSet claims = jwtService.validateAndParse(token);
            String type = (String) claims.getClaim("type");

            // Este filtro processa apenas tokens de viewer
            if (!"viewer".equals(type)) {
                log.debug("Token não é de viewer (type={}), pulando filtro", type);
                chain.doFilter(request, response);
                return;
            }

            String jti = claims.getJWTID();
            log.debug("JWT JTI encontrado: {}", jti);

            ViewerSession session = sessionRepository.findByJti(jti)
                    .orElseThrow(() -> new JwtException("Session not found"));

            if (!session.isValid()) {
                log.warn("Sessão inválida ou expirada para JTI: {}", jti);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session revoked or expired");
                return;
            }

            ViewerAuthentication auth = new ViewerAuthentication(claims, session);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Autenticação configurada com sucesso para JTI: {}", jti);

        } catch (JwtException e) {
            log.warn("Falha na validação do JWT: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }
}
