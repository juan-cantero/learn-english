package com.learntv.api.generation.application.service;

import com.learntv.api.generation.adapter.out.persistence.EpisodeScriptEntity;
import com.learntv.api.generation.adapter.out.persistence.EpisodeScriptRepository;
import com.learntv.api.generation.domain.service.SrtParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing episode scripts.
 * Scripts are stored permanently and used for content generation/regeneration.
 */
@Service
public class EpisodeScriptService {

    private static final Logger log = LoggerFactory.getLogger(EpisodeScriptService.class);

    private final EpisodeScriptRepository repository;
    private final SrtParser srtParser;

    public EpisodeScriptService(EpisodeScriptRepository repository, SrtParser srtParser) {
        this.repository = repository;
        this.srtParser = srtParser;
    }

    /**
     * Get stored parsed script if available.
     *
     * @return the parsed text, or empty if not stored yet
     */
    @Transactional(readOnly = true)
    public Optional<String> getScript(String imdbId, int season, int episode, String language) {
        log.debug("Looking for stored script: {} S{}E{} ({})", imdbId, season, episode, language);

        return repository.findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
                        imdbId, season, episode, language)
                .map(script -> {
                    log.debug("Found stored script for {} S{}E{}", imdbId, season, episode);
                    return script.getParsedText();
                });
    }

    /**
     * Store a newly downloaded script permanently.
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
    public String storeScript(String imdbId, int season, int episode, String language, String rawContent) {
        log.info("Storing script for {} S{}E{} ({})", imdbId, season, episode, language);

        // Parse the SRT content
        String parsedText = srtParser.parsePreservingGroups(rawContent);

        // Check if we already have this stored (shouldn't happen in normal flow)
        Optional<EpisodeScriptEntity> existing = repository
                .findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(imdbId, season, episode, language);

        if (existing.isPresent()) {
            log.warn("Script already exists for {} S{}E{}, skipping", imdbId, season, episode);
            return existing.get().getParsedText();
        }

        // Create new entry
        EpisodeScriptEntity entity = new EpisodeScriptEntity(
                UUID.randomUUID(),
                imdbId,
                season,
                episode,
                language,
                rawContent,
                parsedText,
                Instant.now()
        );

        repository.save(entity);
        log.info("Stored script for {} S{}E{}, {} chars", imdbId, season, episode, parsedText.length());

        return parsedText;
    }

    /**
     * Check if a script exists for the given episode.
     */
    @Transactional(readOnly = true)
    public boolean hasScript(String imdbId, int season, int episode, String language) {
        return repository.existsByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
                imdbId, season, episode, language);
    }
}
