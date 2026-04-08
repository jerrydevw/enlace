package com.enlace.domain.service;

import com.enlace.application.dto.IvsStreamStatusMessage;
import com.enlace.domain.port.in.HandleIvsStreamStatusUseCase;
import com.enlace.domain.port.in.UpdateStreamStatusUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HandleIvsStreamStatusService implements HandleIvsStreamStatusUseCase {

    private final UpdateStreamStatusUseCase updateStreamStatusUseCase;

    public HandleIvsStreamStatusService(UpdateStreamStatusUseCase updateStreamStatusUseCase) {
        this.updateStreamStatusUseCase = updateStreamStatusUseCase;
    }

    @Override
    @Transactional
    public void handle(IvsStreamStatusMessage message) {
        updateStreamStatusUseCase.update(message.channelName(), message.eventName(), message.streamId(), message.recordingS3KeyPrefix());
    }
}
