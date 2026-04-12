package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.exception.PhotoNotFoundException;
import com.enlace.domain.model.CoupleStory;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.ManageEventPhotoUseCase;
import com.enlace.domain.port.in.ServeEventPhotoUseCase;
import com.enlace.domain.port.out.EventRepository;
import com.enlace.infrastructure.aws.S3PhotoAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPhotoService implements ManageEventPhotoUseCase, ServeEventPhotoUseCase {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB

    private final EventRepository eventRepository;
    private final S3PhotoAdapter s3PhotoAdapter;

    // ── Upload ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void upload(UploadPhotoCommand command) {
        validateUpload(command.index(), command.contentType(), command.bytes().length);

        Event event = findEvent(command.eventId());

        String ext = command.contentType().contains("png") ? "png" : "jpg";
        String key = buildKey(command.eventId(), command.index(), ext);

        s3PhotoAdapter.upload(key, command.bytes(), command.contentType());

        CoupleStory story = getOrCreateStory(event);
        story.setPhotoKey(command.index(), key);
        event.setCoupleStory(story);
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);

        log.info("Foto {} salva para evento {}: key={}", command.index(), command.eventId(), key);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(DeletePhotoCommand command) {
        Event event = findEvent(command.eventId());
        CoupleStory story = event.getCoupleStory();
        if (story == null) return;

        String key = story.getPhotoKey(command.index());
        if (key == null || key.isBlank()) return;

        s3PhotoAdapter.delete(key);
        story.setPhotoKey(command.index(), null);
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);

        log.info("Foto {} removida do evento {}", command.index(), command.eventId());
    }

    // ── Serve ────────────────────────────────────────────────────────────────

    @Override
    public PhotoResult serve(String slug, int index) {
        if (index < 1 || index > 3) {
            throw new PhotoNotFoundException("Índice de foto inválido: " + index);
        }

        Event event = eventRepository.findBySlug(slug)
            .orElseThrow(() -> new EventNotFoundException("Evento não encontrado: " + slug));

        CoupleStory story = event.getCoupleStory();
        String key = (story != null) ? story.getPhotoKey(index) : null;

        if (key == null || key.isBlank()) {
            throw new PhotoNotFoundException("Foto não encontrada no índice " + index);
        }

        byte[] bytes = s3PhotoAdapter.download(key);
        String contentType = key.endsWith(".png") ? "image/png" : "image/jpeg";
        return new PhotoResult(bytes, contentType);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateUpload(int index, String contentType, long size) {
        if (index < 1 || index > 3)
            throw new IllegalArgumentException("Índice deve ser 1, 2 ou 3");
        if (!ALLOWED_TYPES.contains(contentType))
            throw new IllegalArgumentException("Tipo não suportado: " + contentType);
        if (size > MAX_BYTES)
            throw new IllegalArgumentException("Arquivo excede o limite de 5 MB");
        if (size == 0)
            throw new IllegalArgumentException("Arquivo vazio");
    }

    private Event findEvent(UUID eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new EventNotFoundException("Evento não encontrado: " + eventId));
    }

    private CoupleStory getOrCreateStory(Event event) {
        return event.getCoupleStory() != null ? event.getCoupleStory() : new CoupleStory();
    }

    private String buildKey(UUID eventId, int index, String ext) {
        return "events/" + eventId + "/photos/" + index + "." + ext;
    }
}
