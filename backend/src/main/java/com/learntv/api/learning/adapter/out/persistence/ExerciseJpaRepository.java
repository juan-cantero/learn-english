package com.learntv.api.learning.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseJpaEntity, UUID> {

    List<ExerciseJpaEntity> findByEpisodeId(UUID episodeId);

    void deleteByEpisodeId(UUID episodeId);
}
