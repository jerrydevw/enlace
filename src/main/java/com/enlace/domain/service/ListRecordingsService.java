package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.ListRecordingsUseCase;
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
    private final IvsGateway ivsGateway;

    @Override
    public List<RecordingResponse> listRecordings(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getRecordingS3Prefix() == null) {
            return List.of();
        }

        List<IvsGateway.S3ObjectInfo> objects = ivsGateway.listObjects(event.getRecordingS3Prefix());

        return objects.stream()
                .filter(obj -> obj.key().endsWith(".mp4") || obj.key().endsWith(".m3u8"))
                .map(obj -> {
                    String filename = obj.key().substring(obj.key().lastIndexOf("/") + 1);
                    String recordingId = Base64.getUrlEncoder().encodeToString(obj.key().getBytes());
                    return new RecordingResponse(
                            recordingId,
                            filename,
                            obj.sizeBytes(),
                            obj.lastModified(),
                            null // URL será gerada sob demanda
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
