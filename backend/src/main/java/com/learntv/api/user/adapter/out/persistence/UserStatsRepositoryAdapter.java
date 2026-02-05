package com.learntv.api.user.adapter.out.persistence;

import com.learntv.api.user.application.port.UserStatsRepository;
import com.learntv.api.user.domain.model.UserStats;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserStatsRepositoryAdapter implements UserStatsRepository {

    private final UserStatsJpaRepository jpaRepository;

    public UserStatsRepositoryAdapter(UserStatsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UserStats> findByUserId(UUID userId) {
        return jpaRepository.findById(userId)
                .map(UserStatsJpaEntity::toDomain);
    }

    @Override
    public UserStats save(UserStats stats) {
        UserStatsJpaEntity entity = UserStatsJpaEntity.fromDomain(stats);
        UserStatsJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
}
