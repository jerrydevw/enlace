package com.enlace.domain.port.out;

import com.enlace.domain.model.EventRecording;

import java.util.List;
import java.util.UUID;

public interface EventRecordingRepository {
    EventRecording save(EventRecording recording);
    List<EventRecording> findByEventId(UUID eventId);
}
