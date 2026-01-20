package com.learntv.api.progress.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressJpaRepository extends JpaRepository<UserProgressJpaEntity, UUID> {

    Optional<UserProgressJpaEntity> findByUserIdAndEpisodeId(String userId, UUID episodeId);

    List<UserProgressJpaEntity> findByUserId(String userId);
}
