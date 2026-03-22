package com.enlace.domain.port.out;

import java.util.UUID;

public interface ProvisioningPublisher {
    void publishProvisioningJob(UUID eventId);
}
