package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.port.in.ListRecordingsUseCase;
import com.enlace.domain.port.out.EventRecordingRepository;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.domain.port.out.IvsGateway;
import com.enlace.infrastructure.web.dto.RecordingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class ListRecordingsService implements ListRecordingsUseCase {

    private final EventRepository eventRepository;
    private final EventRecordingRepository eventRecordingRepository;
    private final IvsGateway ivsGateway;

    @Override
    public List<RecordingResponse> listRecordings(UUID eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        return eventRecordingRepository.findByEventId(eventId).stream()
                .map(recording -> {
                    String recordingId = Base64.getUrlEncoder().encodeToString(recording.getS3Key().getBytes());
                    String downloadUrl = ivsGateway.generatePresignedUrl(recording.getS3Key(), 60);
                    return new RecordingResponse(
                            recordingId,
                            recording.getQuality(),
                            recording.getDurationMs(),
                            recording.getRecordedAt(),
                            downloadUrl
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public String getDownloadUrl(String encodedRecordingId) {
        String key = new String(Base64.getUrlDecoder().decode(encodedRecordingId));
        return ivsGateway.generatePresignedUrl(key, 60);
    }
}
