package com.enlace.domain.port.in;

import java.util.UUID;

public interface ProvisionEventUseCase {
    void provision(UUID eventId);
}
