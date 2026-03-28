package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.exception.ViewerTokenNotFoundException;
import com.enlace.domain.model.ViewerToken;
import com.enlace.domain.port.in.ManageViewerTokensUseCase;
import com.enlace.domain.port.out.CustomerRepository;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.ViewerTokenRepository;
import com.enlace.infrastructure.web.dto.CreateViewerTokenRequest;
import com.enlace.shared.InviteCodeGenerator;
import com.enlace.shared.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageViewerTokensService implements ManageViewerTokensUseCase {

    private final EventRepository eventRepository;
    private final ViewerTokenRepository viewerTokenRepository;
    private final CustomerRepository customerRepository;
    private final PlanLimitsService planLimitsService;
    private final AuditService auditService;

    @Value("${app.viewer-token-ttl-hours:72}")
    private int tokenTtlHours;

    public ManageViewerTokensService(EventRepository eventRepository, 
                                   ViewerTokenRepository viewerTokenRepository,
                                   CustomerRepository customerRepository,
                                   PlanLimitsService planLimitsService,
                                   AuditService auditService) {
        this.eventRepository = eventRepository;
        this.viewerTokenRepository = viewerTokenRepository;
        this.customerRepository = customerRepository;
        this.planLimitsService = planLimitsService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public List<ViewerToken> generateTokens(UUID eventId, List<CreateViewerTokenRequest> requests) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        var customer = customerRepository.findById(event.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found for event"));

        int requestedCount = requests.stream().mapToInt(r -> r.count() != null ? r.count() : 1).sum();
        planLimitsService.validateTokenGeneration(eventId, requestedCount, customer);

        Instant expiresAt = Instant.now().plus(tokenTtlHours, ChronoUnit.HOURS);
        InviteCodeGenerator codeGenerator = new InviteCodeGenerator();

        List<ViewerToken> tokens = requests.stream()
                .flatMap(request -> {
                    int count = request.count() != null ? request.count() : 1;
                    return java.util.stream.IntStream.range(0, count)
                            .mapToObj(i -> {
                                String code = generateUniqueCode(codeGenerator);
                                return new ViewerToken(
                                        UUID.randomUUID(),
                                        eventId,
                                        request.label(),
                                        TokenGenerator.generate(),
                                        code,
                                        expiresAt
                                );
                            });
                })
                .map(viewerTokenRepository::save)
                .collect(Collectors.toList());

        auditService.log(customer.getId(), "TOKEN_GENERATED", "EVENT", eventId, Map.of("count", tokens.size()));

        return tokens;
    }

    private String generateUniqueCode(InviteCodeGenerator codeGenerator) {
        String code;
        int attempts = 0;
        do {
            code = codeGenerator.generate();
            attempts++;
            if (attempts > 5) {
                throw new RuntimeException("Could not generate unique invite code after 5 attempts");
            }
        } while (viewerTokenRepository.existsByCode(code));
        return code;
    }

    @Override
    public List<ViewerToken> getTokensByEventId(UUID eventId) {
        return viewerTokenRepository.findByEventId(eventId);
    }

    @Override
    public Page<ViewerToken> getTokensByEventId(UUID eventId, Pageable pageable) {
        return viewerTokenRepository.findByEventId(eventId, pageable);
    }

    @Override
    @Transactional
    public void revokeToken(UUID eventId, UUID tokenId) {
        ViewerToken token = viewerTokenRepository.findById(tokenId)
                .orElseThrow(() -> new ViewerTokenNotFoundException("Token not found: " + tokenId));
        
        if (!token.getEventId().equals(eventId)) {
             throw new ViewerTokenNotFoundException("Token does not belong to this event");
        }

        token.revoke();
        viewerTokenRepository.save(token);

        var event = eventRepository.findById(eventId).orElseThrow();
        auditService.log(event.getCustomerId(), "TOKEN_REVOKED", "VIEWER_TOKEN", tokenId, null);
    }
}
