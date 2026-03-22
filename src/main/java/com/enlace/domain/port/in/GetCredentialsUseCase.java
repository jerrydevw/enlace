package com.enlace.domain.port.in;

import com.enlace.domain.model.StreamCredential;
import java.util.UUID;

public interface GetCredentialsUseCase {
    StreamCredential getCredentials(UUID eventId);
}
