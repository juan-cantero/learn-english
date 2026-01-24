package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.SubtitleFetchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for fetching episode scripts.
 * First checks if script is stored, then fetches from external API if needed.
 * Scripts are stored permanently for future regeneration.
 */
@Service
public class ScriptFetchService {

    private static final Logger log = LoggerFactory.getLogger(ScriptFetchService.class);

    private final EpisodeScriptService episodeScriptService;
    private final SubtitleFetchPort subtitleFetchPort;

    public ScriptFetchService(EpisodeScriptService episodeScriptService, SubtitleFetchPort subtitleFetchPort) {
        this.episodeScriptService = episodeScriptService;
        this.subtitleFetchPort = subtitleFetchPort;
    }

    /**
     * Fetch the parsed script for an episode.
     * Uses stored script if available, otherwise fetches from OpenSubtitles and stores.
     *
     * @param imdbId   the IMDB ID
     * @param season   season number
     * @param episode  episode number
     * @param language language code (default: "en")
     * @return the parsed dialogue text, or empty if not available
     */
    public Optional<String> fetchScript(String imdbId, int season, int episode, String language) {
        log.info("Fetching script for {} S{}E{} ({})", imdbId, season, episode, language);

        // Check if script is already stored
        Optional<String> stored = episodeScriptService.getScript(imdbId, season, episode, language);
        if (stored.isPresent()) {
            log.info("Returning stored script for {} S{}E{}", imdbId, season, episode);
            return stored;
        }

        // Fetch from external API (first time only)
        log.info("Script not found, fetching from OpenSubtitles for {} S{}E{}", imdbId, season, episode);
        Optional<String> rawContent = subtitleFetchPort.fetchSubtitle(imdbId, season, episode, language);

        if (rawContent.isEmpty()) {
            log.warn("No subtitles found for {} S{}E{}", imdbId, season, episode);
            return Optional.empty();
        }

        // Store permanently and return parsed content
        String parsedText = episodeScriptService.storeScript(imdbId, season, episode, language, rawContent.get());
        return Optional.of(parsedText);
    }

    /**
     * Fetch script with default English language.
     */
    public Optional<String> fetchScript(String imdbId, int season, int episode) {
        return fetchScript(imdbId, season, episode, "en");
    }

    /**
     * Check if a script exists for the given episode.
     * Useful for determining if CREATE or RECREATE should be shown.
     */
    public boolean hasScript(String imdbId, int season, int episode, String language) {
        return episodeScriptService.hasScript(imdbId, season, episode, language);
    }
}
