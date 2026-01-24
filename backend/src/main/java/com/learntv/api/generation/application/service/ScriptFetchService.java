package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.SubtitleFetchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for fetching episode scripts with caching.
 * First checks the cache, then fetches from external API if needed.
 */
@Service
public class ScriptFetchService {

    private static final Logger log = LoggerFactory.getLogger(ScriptFetchService.class);

    private final ScriptCacheService cacheService;
    private final SubtitleFetchPort subtitleFetchPort;

    public ScriptFetchService(ScriptCacheService cacheService, SubtitleFetchPort subtitleFetchPort) {
        this.cacheService = cacheService;
        this.subtitleFetchPort = subtitleFetchPort;
    }

    /**
     * Fetch the parsed script for an episode.
     * Uses cache if available, otherwise fetches from OpenSubtitles and caches.
     *
     * @param imdbId   the IMDB ID
     * @param season   season number
     * @param episode  episode number
     * @param language language code (default: "en")
     * @return the parsed dialogue text, or empty if not available
     */
    public Optional<String> fetchScript(String imdbId, int season, int episode, String language) {
        log.info("Fetching script for {} S{}E{} ({})", imdbId, season, episode, language);

        // Check cache first
        Optional<String> cached = cacheService.getCachedScript(imdbId, season, episode, language);
        if (cached.isPresent()) {
            log.info("Returning cached script for {} S{}E{}", imdbId, season, episode);
            return cached;
        }

        // Fetch from external API
        log.info("Cache miss, fetching from OpenSubtitles for {} S{}E{}", imdbId, season, episode);
        Optional<String> rawContent = subtitleFetchPort.fetchSubtitle(imdbId, season, episode, language);

        if (rawContent.isEmpty()) {
            log.warn("No subtitles found for {} S{}E{}", imdbId, season, episode);
            return Optional.empty();
        }

        // Cache and return parsed content
        String parsedText = cacheService.cacheScript(imdbId, season, episode, language, rawContent.get());
        return Optional.of(parsedText);
    }

    /**
     * Fetch script with default English language.
     */
    public Optional<String> fetchScript(String imdbId, int season, int episode) {
        return fetchScript(imdbId, season, episode, "en");
    }
}
