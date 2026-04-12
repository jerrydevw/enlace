package com.enlace.domain.port.in;

import com.enlace.domain.model.EventStatus;
import java.time.Instant;

public interface ValidateInviteCodeUseCase {
    ViewerSessionResult validate(ValidateInviteCommand command);

    record ValidateInviteCommand(
        String eventSlug,
        String code,
        String inviteToken,
        String ipAddress,
        String userAgent
    ) {}

    record ViewerSessionResult(
        String sessionToken,
        Instant expiresAt,
        String eventTitle,
        EventStatus eventStatus,
        Instant scheduledAt,
        String watchNonce
    ) {}
}
