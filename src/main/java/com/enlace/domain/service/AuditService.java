package com.enlace.domain.service;

import com.enlace.infrastructure.persistence.AuditLogEntity;
import com.enlace.infrastructure.persistence.SpringDataAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final SpringDataAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(UUID customerId, String action, String resourceType, UUID resourceId, Map<String, Object> details) {
        try {
            AuditLogEntity entity = new AuditLogEntity();
            entity.setId(UUID.randomUUID());
            entity.setCustomerId(customerId);
            entity.setAction(action);
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setTimestamp(Instant.now());
            
            if (details != null) {
                entity.setDetails(objectMapper.writeValueAsString(details));
            }

            auditLogRepository.save(entity);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria: {}", e.getMessage(), e);
        }
    }
}
