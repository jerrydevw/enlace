package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.StreamCredential;
import com.enlace.domain.port.in.ProvisionEventUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.IvsGateway;
import com.enlace.domain.port.out.StreamCredentialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class ProvisionEventService implements ProvisionEventUseCase {

    private final EventRepository eventRepository;
    private final StreamCredentialRepository streamCredentialRepository;
    private final IvsGateway ivsGateway;

    @Value("${aws.account-id:}")
    private String accountId;

    public ProvisionEventService(EventRepository eventRepository,
                                 StreamCredentialRepository streamCredentialRepository,
                                 IvsGateway ivsGateway, @Value("${aws.ivs.recording-bucket}") String recordingBucket
    ) {
        this.eventRepository = eventRepository;
        this.streamCredentialRepository = streamCredentialRepository;
        this.ivsGateway = ivsGateway;
    }

    @Override
    @Transactional
    public void provision(UUID eventId) {
        log.info("Iniciando provisionamento para o evento: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        try {
            // Idempotency check: if channel already created, check next steps
            if (event.getIvsChannelArn() == null) {
                log.info("Criando canal IVS para o evento: {}", event.getSlug());
                event.markProvisioning();
                eventRepository.save(event);

                IvsGateway.IvsChannelResult result = ivsGateway.createChannel(event.getSlug(), event.getPlan());
                log.info("Canal IVS criado: ARN={}, Endpoint={}", result.channelArn(), result.ingestEndpoint());

                String channelId = result.channelArn().substring(result.channelArn().lastIndexOf('/') + 1);
                
                if (accountId == null || accountId.isBlank()) {
                    log.error("aws.account-id não configurado — o prefixo S3 ficará incorreto e gravações não serão encontradas. Configure AWS_ACCOUNT_ID.");
                }
                String accountIdToUse = (accountId == null || accountId.isBlank()) ? "" : accountId;
                String s3Prefix = "ivs/v1/" + accountIdToUse + "/" + channelId;
                s3Prefix = s3Prefix.replace("//", "/");

                event.markReady(result.channelArn(), result.ingestEndpoint(), result.playbackUrl(), s3Prefix);
                eventRepository.save(event);

                StreamCredential credential = new StreamCredential(
                        UUID.randomUUID(),
                        event.getId(),
                        result.streamKeyArn(),
                        result.ingestEndpoint(),
                        result.streamKey(),
                        null // expiresAt nullable
                );
                streamCredentialRepository.save(credential);
                log.info("Provisionamento concluído com sucesso para o evento: {}", eventId);
            } else {
                log.info("Evento {} já possui canal IVS configurado: {}", eventId, event.getIvsChannelArn());
                // Already has ARN, check if StreamCredential exists
                if (streamCredentialRepository.findByEventId(eventId).isEmpty()) {
                    log.warn("Evento possui ARN mas não possui credenciais salvas: {}", eventId);
                }
            }
        } catch (Exception e) {
            log.error("Erro durante o provisionamento do evento {}: {}", eventId, e.getMessage(), e);
            event.markProvisioningFailed();
            eventRepository.save(event);
            throw e;
        }
    }
}
