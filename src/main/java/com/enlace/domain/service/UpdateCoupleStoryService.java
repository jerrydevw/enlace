package com.enlace.domain.service;

import com.enlace.domain.exception.EventNotFoundException;
import com.enlace.domain.model.CoupleStory;
import com.enlace.domain.model.Event;
import com.enlace.domain.port.in.UpdateCoupleStoryUseCase;
import com.enlace.domain.port.out.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UpdateCoupleStoryService implements UpdateCoupleStoryUseCase {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void update(UpdateCoupleStoryCommand command) {
        Event event = eventRepository.findById(command.eventId())
            .orElseThrow(() -> new EventNotFoundException(
                "Event not found: " + command.eventId()));

        CoupleStory story = new CoupleStory(
            sanitize(command.partner1Name(), 100),
            sanitize(command.partner2Name(), 100),
            sanitize(command.message(), 400)
        );

        event.setCoupleStory(story.hasContent() ? story : null);
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);
    }

    /** Trim, null se blank, trunca no limite */
    private String sanitize(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
