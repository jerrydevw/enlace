package com.enlace.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

        log.debug("CustomerJwtAuthFilter processando: {} {}", request.getMethod(), request.getRequestURI());

        String token = extractToken(request);
        if (token == null) {
            log.debug("Nenhum token encontrado (header ou cookie)");
            chain.doFilter(request, response);
            return;
        }

        log.debug("Token encontrado, tentando decodificar...");

        try {
            Jwt jwt = jwtService.decode(token);
            log.debug("Token decodificado com sucesso. Type: {}, Sub: {}", jwt.getClaim("type"), jwt.getSubject());

            // Verifica se é um token de acesso (não de refresh)
            if (jwt.getClaim("type") != null && "REFRESH".equals(jwt.getClaim("type"))) {
                log.debug("Token é REFRESH, ignorando");
                chain.doFilter(request, response);
                return;
            }

            CustomerAuthentication auth = new CustomerAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("CustomerAuthentication configurada no SecurityContext");

        } catch (JwtException e) {
            log.warn("Falha ao decodificar token JWT: {}", e.getMessage(), e);
            // Se falhar aqui, pode ser um token de Viewer, então deixamos passar para o próximo filtro
        } catch (Exception e) {
            log.error("Erro inesperado ao processar token: {}", e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Tenta extrair do header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Tenta extrair do cookie access_token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
