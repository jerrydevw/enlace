package com.enlace.infrastructure.persistence;

import com.enlace.domain.model.Event;
import com.enlace.domain.port.out.EventRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaEventRepository implements EventRepository {

    private final SpringDataEventRepository springDataEventRepository;

    public JpaEventRepository(SpringDataEventRepository springDataEventRepository) {
        this.springDataEventRepository = springDataEventRepository;
    }

    @Override
    public Event save(Event event) {
        EventEntity entity = EventEntity.fromDomain(event);
        return springDataEventRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return springDataEventRepository.findById(id).map(EventEntity::toDomain);
    }

    @Override
    public Optional<Event> findBySlug(String slug) {
        return springDataEventRepository.findBySlug(slug).map(EventEntity::toDomain);
    }

    @Override
    public void delete(Event event) {
        springDataEventRepository.delete(EventEntity.fromDomain(event));
    }
}
