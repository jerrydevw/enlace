package com.enlace.domain.port.in;

import com.enlace.application.dto.IvsStreamStatusMessage;

public interface HandleIvsStreamStatusUseCase {
    void handle(IvsStreamStatusMessage message);
}
