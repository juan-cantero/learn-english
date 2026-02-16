package com.learntv.api.learning.application.usecase;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.model.Show;
import com.learntv.api.generation.adapter.out.persistence.EpisodeScriptEntity;
import com.learntv.api.generation.adapter.out.persistence.EpisodeScriptRepository;
import com.learntv.api.generation.application.port.out.ShadowingExtractionPort;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.generation.domain.model.ExtractedScene;
import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.application.port.ShadowingSceneRepository;
import com.learntv.api.learning.application.port.ShadowingSceneRepository.ShadowingScene;
import com.learntv.api.learning.domain.exception.EpisodeNotFoundException;
import com.learntv.api.learning.domain.model.Episode;
import com.learntv.api.learning.domain.model.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GetShadowingScenesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetShadowingScenesUseCase.class);

    private final ShadowingSceneRepository shadowingSceneRepository;
    private final EpisodeRepository episodeRepository;
    private final ShowRepository showRepository;
    private final ShowMetadataPort showMetadataPort;
    private final EpisodeScriptRepository episodeScriptRepository;
    private final LessonQueryPort lessonQueryPort;
    private final ShadowingExtractionPort shadowingExtractionPort;

    public GetShadowingScenesUseCase(ShadowingSceneRepository shadowingSceneRepository,
                                      EpisodeRepository episodeRepository,
                                      ShowRepository showRepository,
                                      ShowMetadataPort showMetadataPort,
                                      EpisodeScriptRepository episodeScriptRepository,
                                      LessonQueryPort lessonQueryPort,
                                      ShadowingExtractionPort shadowingExtractionPort) {
        this.shadowingSceneRepository = shadowingSceneRepository;
        this.episodeRepository = episodeRepository;
        this.showRepository = showRepository;
        this.showMetadataPort = showMetadataPort;
        this.episodeScriptRepository = episodeScriptRepository;
        this.lessonQueryPort = lessonQueryPort;
        this.shadowingExtractionPort = shadowingExtractionPort;
    }

    public List<ShadowingScene> execute(String showSlug, String episodeSlug) {
        log.info("Shadowing request for show={}, episode={}", showSlug, episodeSlug);

        Episode episode = episodeRepository.findByShowSlugAndEpisodeSlug(showSlug, episodeSlug)
                .orElseThrow(() -> new EpisodeNotFoundException(showSlug, episodeSlug));
        log.info("Found episode: id={}, S{}E{}", episode.getId().value(),
                episode.getSeasonNumber(), episode.getEpisodeNumber());

        // Check if scenes already exist (cached)
        if (shadowingSceneRepository.existsByEpisodeId(episode.getId().value())) {
            log.info("Returning cached shadowing scenes for {}", episodeSlug);
            return shadowingSceneRepository.findByEpisodeId(episode.getId().value());
        }

        // Need to generate scenes
        log.info("Generating shadowing scenes for {}", episodeSlug);

        // Get the show's TMDB ID to look up the IMDB ID for the script
        Show show = showRepository.findBySlug(showSlug)
                .orElseThrow(() -> {
                    log.error("Show not found: {}", showSlug);
                    return new RuntimeException("Show not found: " + showSlug);
                });
        log.info("Found show: tmdbId={}", show.getTmdbId());

        if (show.getTmdbId() == null) {
            log.error("Show has no TMDB ID: {}", showSlug);
            throw new RuntimeException("Show has no TMDB ID, cannot look up script: " + showSlug);
        }

        String imdbId = showMetadataPort.getImdbId(
                show.getTmdbId(), episode.getSeasonNumber(), episode.getEpisodeNumber());
        log.info("Resolved IMDB ID: {}", imdbId);

        // Fetch the raw SRT script
        EpisodeScriptEntity script = episodeScriptRepository
                .findByImdbIdAndSeasonNumberAndEpisodeNumberAndLanguage(
                        imdbId, episode.getSeasonNumber(), episode.getEpisodeNumber(), "en")
                .orElseThrow(() -> {
                    log.error("No script found for imdbId={}, S{}E{}", imdbId,
                            episode.getSeasonNumber(), episode.getEpisodeNumber());
                    return new RuntimeException("No script found for episode: " + episodeSlug);
                });
        log.info("Found script: {} chars raw content", script.getRawContent().length());

        // Get vocabulary terms and expressions from the lesson
        Lesson lesson = lessonQueryPort.loadFullLesson(showSlug, episodeSlug)
                .orElseThrow(() -> new EpisodeNotFoundException(showSlug, episodeSlug));

        List<String> vocabTerms = lesson.getVocabulary().stream()
                .map(v -> v.getTerm())
                .toList();

        List<String> expressions = lesson.getExpressions().stream()
                .map(e -> e.getPhrase())
                .toList();
        log.info("Lesson has {} vocab terms, {} expressions", vocabTerms.size(), expressions.size());

        // Call AI to extract scenes
        log.info("Calling AI to extract shadowing scenes...");
        List<ExtractedScene> extractedScenes = shadowingExtractionPort
                .extractShadowingScenes(script.getRawContent(), vocabTerms, expressions);
        log.info("AI returned {} scenes", extractedScenes.size());

        // Persist and return
        shadowingSceneRepository.saveAll(episode.getId().value(), extractedScenes);
        return shadowingSceneRepository.findByEpisodeId(episode.getId().value());
    }
}
