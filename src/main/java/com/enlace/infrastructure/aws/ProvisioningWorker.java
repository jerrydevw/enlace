package com.enlace.infrastructure.aws;

import com.enlace.domain.port.in.ProvisionEventUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProvisioningWorker {

    private final ProvisionEventUseCase provisionEventUseCase;

    public ProvisioningWorker(ProvisionEventUseCase provisionEventUseCase) {
        this.provisionEventUseCase = provisionEventUseCase;
    }

    @SqsListener("${aws.sqs.provisioning-queue-url}")
    public void listen(String message) {
        log.info("Mensagem SQS recebida para provisionamento: {}", message);
        try {
            UUID eventId = UUID.fromString(message);
            provisionEventUseCase.provision(eventId);
            log.info("Processamento da mensagem concluído com sucesso: {}", message);
        } catch (IllegalArgumentException e) {
            log.error("Formato de ID inválido na mensagem SQS: {}", message);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem de provisionamento {}: {}", message, e.getMessage());
            throw e;
        }
    }
}