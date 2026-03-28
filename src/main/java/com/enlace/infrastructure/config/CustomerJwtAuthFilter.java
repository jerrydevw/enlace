package com.enlace.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerJwtAuthFilter extends OncePerRequestFilter {

    private final CustomerJwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Jwt jwt = jwtService.decode(token);
            
            // Verifica se é um token de acesso (não de refresh)
            if (jwt.getClaim("type") != null && "REFRESH".equals(jwt.getClaim("type"))) {
                chain.doFilter(request, response);
                return;
            }

            CustomerAuthentication auth = new CustomerAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
        } catch (JwtException e) {
            log.debug("Token JWT de customer inválido ou não destinado a este filtro: {}", e.getMessage());
            // Se falhar aqui, pode ser um token de Viewer, então deixamos passar para o próximo filtro
        }

        chain.doFilter(request, response);
    }
}
