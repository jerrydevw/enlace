package com.enlace.domain.port.in;

import java.util.UUID;

public interface DeleteEventUseCase {
    void delete(UUID id);
}
