package com.learntv.api.learning.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VocabularyJpaRepository extends JpaRepository<VocabularyJpaEntity, UUID> {

    List<VocabularyJpaEntity> findByEpisodeId(UUID episodeId);

    void deleteByEpisodeId(UUID episodeId);
}
