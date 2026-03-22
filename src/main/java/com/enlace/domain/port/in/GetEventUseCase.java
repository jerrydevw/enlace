package com.enlace.domain.port.in;

import com.enlace.domain.model.Event;
import java.util.UUID;

public interface GetEventUseCase {
    Event getById(UUID id);
}
