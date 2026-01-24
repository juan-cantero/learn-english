package com.learntv.api.generation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CachedScriptRepository extends JpaRepository<CachedScriptEntity, UUID> {

    /**
     * Find a cached script by episode identifiers.
     */
    Optional<CachedScriptEntity> findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
            String imdbId, int seasonNumber, int episodeNumber, String language);

    /**
     * Check if a valid (non-expired) cache exists.
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM CachedScriptEntity c " +
           "WHERE c.imdbId = :imdbId AND c.seasonNumber = :seasonNumber " +
           "AND c.episodeNumber = :episodeNumber AND c.language = :language " +
           "AND c.expiresAt > :now")
    boolean existsValidCache(String imdbId, int seasonNumber, int episodeNumber,
                             String language, Instant now);

    /**
     * Delete expired cache entries.
     */
    @Modifying
    @Query("DELETE FROM CachedScriptEntity c WHERE c.expiresAt < :now")
    int deleteExpiredEntries(Instant now);
}
