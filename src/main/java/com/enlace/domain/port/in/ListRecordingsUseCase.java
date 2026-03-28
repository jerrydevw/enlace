package com.enlace.domain.port.in;

import com.enlace.infrastructure.web.dto.RecordingResponse;
import java.util.List;
import java.util.UUID;

public interface ListRecordingsUseCase {
    List<RecordingResponse> listRecordings(UUID eventId);
    String getDownloadUrl(String recordingId);
}
