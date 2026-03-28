package com.enlace.infrastructure.messaging;

import com.enlace.application.dto.IvsStreamStatusMessage;
import com.enlace.domain.port.in.HandleIvsStreamStatusUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IvsEventsListener {

    private static final Logger log = LoggerFactory.getLogger(IvsEventsListener.class);

    private final HandleIvsStreamStatusUseCase handleIvsStreamStatus;

    public IvsEventsListener(HandleIvsStreamStatusUseCase handleIvsStreamStatus) {
        this.handleIvsStreamStatus = handleIvsStreamStatus;
    }

    @SqsListener("${aws.sqs.ivs-events-queue-url}")
    public void onIvsEvent(IvsStreamStatusMessage message) {
        log.info("Mensagem recebida da fila IVS: channel={} event={}",
            message.channelName(), message.eventName());
        handleIvsStreamStatus.handle(message);
    }
}
