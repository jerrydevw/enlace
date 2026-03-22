package com.enlace.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyInterceptor.class);
    private static final String HEADER = "X-Api-Key";

    // Injetada via application.properties ou variável de ambiente ENLACE_API_KEY
    // Troca para Secrets Manager / RDS quando estiver pronto
    @Value("${api.key}")
    private String validApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String key = request.getHeader(HEADER);

        if (key == null || !key.equals(validApiKey)) {
            log.warn("Requisição bloqueada — API Key inválida ou ausente. ip={} path={}",
                    request.getRemoteAddr(), request.getRequestURI());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"error": "Unauthorized", "message": "API Key inválida ou ausente"}
                    """);
            return false;
        }

        return true;
    }
}