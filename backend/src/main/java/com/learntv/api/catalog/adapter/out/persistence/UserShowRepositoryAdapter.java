package com.learntv.api.catalog.adapter.out.persistence;

import com.learntv.api.catalog.application.port.UserShowRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserShowRepositoryAdapter implements UserShowRepository {

    private final UserShowJpaRepository jpaRepository;

    public UserShowRepositoryAdapter(UserShowJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void addUserShow(UUID userId, UUID showId) {
        // Check if already exists to avoid duplicate key exceptions
        if (jpaRepository.existsByUserIdAndShowId(userId, showId)) {
            return; // Already exists, silently ignore
        }

        try {
            UserShowJpaEntity entity = UserShowJpaEntity.create(userId, showId);
            jpaRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // Race condition: another thread added it between check and save
            // Silently ignore as the end result is the same
        }
    }

    @Override
    public List<UUID> findShowIdsByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(UserShowJpaEntity::getShowId)
                .toList();
    }
}
