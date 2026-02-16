package com.learntv.api.learning.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShadowingSceneJpaRepository extends JpaRepository<ShadowingSceneJpaEntity, UUID> {

    List<ShadowingSceneJpaEntity> findByEpisodeIdOrderBySceneIndex(UUID episodeId);

    boolean existsByEpisodeId(UUID episodeId);
}
