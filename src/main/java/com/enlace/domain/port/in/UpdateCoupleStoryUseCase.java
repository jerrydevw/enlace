package com.enlace.domain.port.in;

import java.util.UUID;

public interface UpdateCoupleStoryUseCase {

    void update(UpdateCoupleStoryCommand command);

    record UpdateCoupleStoryCommand(
        UUID eventId,
        UUID customerId,
        String partner1Name,
        String partner2Name,
        String message
    ) {}
}
