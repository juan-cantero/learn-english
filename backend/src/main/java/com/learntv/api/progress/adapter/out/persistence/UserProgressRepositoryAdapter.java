package com.learntv.api.progress.adapter.out.persistence;

import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.UserProgress;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserProgressRepositoryAdapter implements UserProgressRepository {

    private final UserProgressJpaRepository jpaRepository;

    public UserProgressRepositoryAdapter(UserProgressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UserProgress> findByUserIdAndEpisodeId(String userId, UUID episodeId) {
        return jpaRepository.findByUserIdAndEpisodeId(userId, episodeId)
                .map(UserProgressJpaEntity::toDomain);
    }

    @Override
    public List<UserProgress> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(UserProgressJpaEntity::toDomain)
                .toList();
    }

    @Override
    public UserProgress save(UserProgress progress) {
        UserProgressJpaEntity entity = UserProgressJpaEntity.fromDomain(progress);
        UserProgressJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
}
