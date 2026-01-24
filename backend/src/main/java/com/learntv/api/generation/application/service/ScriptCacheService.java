package com.learntv.api.generation.application.service;

import com.learntv.api.generation.adapter.out.persistence.CachedScriptEntity;
import com.learntv.api.generation.adapter.out.persistence.CachedScriptRepository;
import com.learntv.api.generation.domain.service.SrtParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for caching episode scripts/subtitles.
 * Handles storing, retrieving, and cleaning up cached scripts.
 */
@Service
public class ScriptCacheService {

    private static final Logger log = LoggerFactory.getLogger(ScriptCacheService.class);
    private static final int CACHE_TTL_DAYS = 30;

    private final CachedScriptRepository repository;
    private final SrtParser srtParser;

    public ScriptCacheService(CachedScriptRepository repository, SrtParser srtParser) {
        this.repository = repository;
        this.srtParser = srtParser;
    }

    /**
     * Get cached parsed script if available and not expired.
     *
     * @return the parsed text, or empty if not cached or expired
     */
    @Transactional(readOnly = true)
    public Optional<String> getCachedScript(String imdbId, int season, int episode, String language) {
        log.debug("Looking for cached script: {} S{}E{} ({})", imdbId, season, episode, language);

        return repository.findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
                        imdbId, season, episode, language)
                .filter(cached -> !cached.isExpired())
                .map(cached -> {
                    log.debug("Cache hit for {} S{}E{}", imdbId, season, episode);
                    return cached.getParsedText();
                });
    }

    /**
     * Cache a newly downloaded script.
     * Parses the SRT content and stores both raw and parsed versions.
     *
     * @param imdbId     the IMDB ID
     * @param season     season number
     * @param episode    episode number
     * @param language   language code
     * @param rawContent the raw SRT content
     * @return the parsed text
     */
    @Transactional
    public String cacheScript(String imdbId, int season, int episode, String language, String rawContent) {
        log.info("Caching script for {} S{}E{} ({})", imdbId, season, episode, language);

        // Parse the SRT content
        String parsedText = srtParser.parsePreservingGroups(rawContent);

        // Check if we already have this cached (shouldn't happen, but handle it)
        Optional<CachedScriptEntity> existing = repository
                .findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(imdbId, season, episode, language);

        if (existing.isPresent()) {
            log.debug("Removing existing cache entry before updating");
            repository.delete(existing.get());
        }

        // Create new cache entry
        Instant now = Instant.now();
        Instant expiresAt = now.plus(CACHE_TTL_DAYS, ChronoUnit.DAYS);

        CachedScriptEntity entity = new CachedScriptEntity(
                UUID.randomUUID(),
                imdbId,
                season,
                episode,
                language,
                rawContent,
                parsedText,
                now,
                expiresAt
        );

        repository.save(entity);
        log.info("Cached script for {} S{}E{}, expires at {}", imdbId, season, episode, expiresAt);

        return parsedText;
    }

    /**
     * Check if a valid cache exists for the given episode.
     */
    @Transactional(readOnly = true)
    public boolean hasCachedScript(String imdbId, int season, int episode, String language) {
        return repository.existsValidCache(imdbId, season, episode, language, Instant.now());
    }

    /**
     * Scheduled task to clean up expired cache entries.
     * Runs daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredEntries() {
        log.info("Running scheduled cache cleanup");
        int deleted = repository.deleteExpiredEntries(Instant.now());
        log.info("Deleted {} expired cache entries", deleted);
    }
}
