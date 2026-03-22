package com.enlace.infrastructure.aws;

import com.enlace.domain.port.in.ProvisionEventUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProvisioningWorker {

    private final ProvisionEventUseCase provisionEventUseCase;

    public ProvisioningWorker(ProvisionEventUseCase provisionEventUseCase) {
        this.provisionEventUseCase = provisionEventUseCase;
    }

    @SqsListener("${aws.sqs.provisioning-queue-url}")
    public void listen(String message) {
        UUID eventId = UUID.fromString(message);
        provisionEventUseCase.provision(eventId);
        // delete é automático se não lançar exception
    }
}