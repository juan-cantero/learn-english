package com.learntv.api.learning.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EpisodeJpaRepository extends JpaRepository<EpisodeJpaEntity, UUID> {

    List<EpisodeJpaEntity> findByShowIdOrderBySeasonNumberAscEpisodeNumberAsc(UUID showId);

    List<EpisodeJpaEntity> findByShowSlugOrderBySeasonNumberAscEpisodeNumberAsc(String showSlug);

    // No cross-context join - queries within learning context only
    Optional<EpisodeJpaEntity> findByShowSlugAndSlug(String showSlug, String slug);

    Optional<EpisodeJpaEntity> findByShowIdAndSeasonNumberAndEpisodeNumber(
            UUID showId, int seasonNumber, int episodeNumber);
}
