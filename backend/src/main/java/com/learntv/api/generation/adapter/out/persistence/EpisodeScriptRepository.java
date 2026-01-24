package com.learntv.api.generation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeScriptRepository extends JpaRepository<EpisodeScriptEntity, UUID> {

    /**
     * Find a stored script by episode identifiers.
     */
    Optional<EpisodeScriptEntity> findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
            String imdbId, int seasonNumber, int episodeNumber, String language);

    /**
     * Check if a script exists for the given episode.
     */
    boolean existsByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
            String imdbId, int seasonNumber, int episodeNumber, String language);
}
