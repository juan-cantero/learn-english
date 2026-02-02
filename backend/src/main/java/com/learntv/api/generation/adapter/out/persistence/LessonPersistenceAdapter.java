package com.learntv.api.generation.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.catalog.adapter.out.persistence.ShowJpaEntity;
import com.learntv.api.catalog.adapter.out.persistence.ShowJpaRepository;
import com.learntv.api.catalog.domain.model.*;
import com.learntv.api.generation.application.port.in.GenerationCommand;
import com.learntv.api.generation.application.port.out.LessonPersistencePort;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.generation.domain.model.*;
import com.learntv.api.learning.adapter.out.persistence.*;
import com.learntv.api.learning.domain.model.ExerciseType;
import com.learntv.api.learning.domain.model.VocabularyCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Adapter that persists a GeneratedLesson to the database.
 *
 * This adapter bridges the generation bounded context with the learning
 * bounded context by transforming GeneratedLesson domain models into
 * JPA entities for persistence.
 */
@Component
public class LessonPersistenceAdapter implements LessonPersistencePort {

    private static final Logger log = LoggerFactory.getLogger(LessonPersistenceAdapter.class);

    private final ShowMetadataPort showMetadataPort;
    private final ShowJpaRepository showRepository;
    private final EpisodeJpaRepository episodeRepository;
    private final VocabularyJpaRepository vocabularyRepository;
    private final GrammarPointJpaRepository grammarRepository;
    private final ExpressionJpaRepository expressionRepository;
    private final ExerciseJpaRepository exerciseRepository;
    private final ObjectMapper objectMapper;

    public LessonPersistenceAdapter(
            ShowMetadataPort showMetadataPort,
            ShowJpaRepository showRepository,
            EpisodeJpaRepository episodeRepository,
            VocabularyJpaRepository vocabularyRepository,
            GrammarPointJpaRepository grammarRepository,
            ExpressionJpaRepository expressionRepository,
            ExerciseJpaRepository exerciseRepository,
            ObjectMapper objectMapper) {
        this.showMetadataPort = showMetadataPort;
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.grammarRepository = grammarRepository;
        this.expressionRepository = expressionRepository;
        this.exerciseRepository = exerciseRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public UUID save(
            GeneratedLesson lesson,
            String tmdbId,
            String imdbId,
            int seasonNumber,
            int episodeNumber,
            String episodeTitle,
            String genre,
            String imageUrl) {

        log.info("Saving lesson for TMDB ID: {}, IMDB ID: {}, S{}E{}",
                tmdbId, imdbId, seasonNumber, episodeNumber);

        // 1. Find or create show
        ShowJpaEntity show = findOrCreateShow(tmdbId, genre, imageUrl);
        log.info("Using show: {} ({})", show.getTitle(), show.getId());

        // 2. Check if episode already exists
        var existingEpisode = episodeRepository.findByShowIdAndSeasonNumberAndEpisodeNumber(
                show.getId(), seasonNumber, episodeNumber);

        if (existingEpisode.isPresent()) {
            log.warn("Episode already exists: {} S{}E{} - deleting existing content",
                    show.getTitle(), seasonNumber, episodeNumber);
            deleteExistingEpisodeContent(existingEpisode.get().getId());
            episodeRepository.delete(existingEpisode.get());
        }

        // 3. Create episode
        EpisodeJpaEntity episode = EpisodeJpaEntity.create(
                show.getId(),
                show.getSlug(),
                seasonNumber,
                episodeNumber,
                episodeTitle != null ? episodeTitle : "Episode " + episodeNumber,
                "Generated lesson for " + show.getTitle()
        );
            episodeRepository.save(episode);
            log.info("Created episode: {}", episode.getSlug());

            // 4. Save vocabulary
            for (ExtractedVocabulary v : lesson.vocabulary()) {
                VocabularyJpaEntity vocab = VocabularyJpaEntity.create(
                        episode.getId(),
                        v.term(),
                        v.definition(),
                        v.phonetic(),
                        VocabularyCategory.fromString(v.category()),
                        v.exampleSentence(),
                        v.audioUrl()
                );
                vocabularyRepository.save(vocab);
            }
            log.info("Saved {} vocabulary items", lesson.vocabulary().size());

            // 5. Save grammar points
            for (ExtractedGrammar g : lesson.grammarPoints()) {
                String examples = g.examples() != null ? String.join("; ", g.examples()) : "";
                GrammarPointJpaEntity grammar = GrammarPointJpaEntity.create(
                        episode.getId(),
                        g.title(),
                        g.explanation(),
                        g.structure(),
                        examples
                );
                grammarRepository.save(grammar);
            }
            log.info("Saved {} grammar points", lesson.grammarPoints().size());

            // 6. Save expressions
            for (ExtractedExpression e : lesson.expressions()) {
                ExpressionJpaEntity expression = ExpressionJpaEntity.create(
                        episode.getId(),
                        e.phrase(),
                        e.meaning(),
                        e.context(),
                        e.usageNote(),
                        e.audioUrl()
                );
                expressionRepository.save(expression);
            }
            log.info("Saved {} expressions", lesson.expressions().size());

            // 7. Save exercises
            for (GeneratedExercise ex : lesson.exercises()) {
                String optionsJson = null;
                if (ex.options() != null) {
                    try {
                        optionsJson = objectMapper.writeValueAsString(ex.options());
                    } catch (JsonProcessingException e) {
                        optionsJson = String.join(",", ex.options());
                    }
                }
                ExerciseJpaEntity exercise = ExerciseJpaEntity.create(
                        episode.getId(),
                        ExerciseType.valueOf(ex.type()),
                        ex.question(),
                        ex.correctAnswer(),
                        optionsJson,
                        ex.points(),
                        null // audioUrl generated separately
                );
                exerciseRepository.save(exercise);
            }
            log.info("Saved {} exercises", lesson.exercises().size());

        return episode.getId();
    }

    private void deleteExistingEpisodeContent(UUID episodeId) {
        vocabularyRepository.deleteByEpisodeId(episodeId);
        grammarRepository.deleteByEpisodeId(episodeId);
        expressionRepository.deleteByEpisodeId(episodeId);
        exerciseRepository.deleteByEpisodeId(episodeId);
    }

    private ShowJpaEntity findOrCreateShow(String tmdbId, String genre, String imageUrl) {
        // Fetch show details from TMDB
        var showWithSeasons = showMetadataPort.getShowWithSeasons(tmdbId);

        String showTitle;
        String description;
        String posterUrl;
        int totalSeasons;
        int totalEpisodes;

        if (showWithSeasons.isPresent()) {
            var tmdbShow = showWithSeasons.get();
            showTitle = tmdbShow.title();
            description = tmdbShow.overview() != null ? tmdbShow.overview() : "TV Show";
            posterUrl = tmdbShow.posterUrl();
            totalSeasons = tmdbShow.seasons().size();
            totalEpisodes = tmdbShow.seasons().stream()
                    .mapToInt(ShowMetadataPort.Season::episodeCount)
                    .sum();
            log.info("Fetched show metadata from TMDB: {} ({} seasons, {} episodes)",
                    showTitle, totalSeasons, totalEpisodes);
        } else {
            // Fallback if TMDB fetch fails
            log.warn("Could not fetch show metadata from TMDB for ID: {}", tmdbId);
            showTitle = "Show " + tmdbId;
            description = "TV Show";
            posterUrl = imageUrl;
            totalSeasons = 1;
            totalEpisodes = 1;
        }

        String slug = generateSlug(showTitle);

        return showRepository.findBySlug(slug)
                .orElseGet(() -> {
                    log.info("Creating new show: {}", showTitle);
                    Show show = Show.builder()
                            .id(ShowId.of(UUID.randomUUID()))
                            .title(showTitle)
                            .slug(slug)
                            .description(description)
                            .genre(mapGenre(genre))
                            .accent(AccentType.AMERICAN)
                            .difficulty(DifficultyLevel.INTERMEDIATE)
                            .imageUrl(posterUrl)
                            .totalSeasons(totalSeasons)
                            .totalEpisodes(totalEpisodes)
                            .build();
                    return showRepository.save(ShowJpaEntity.fromDomain(show));
                });
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private Genre mapGenre(String genre) {
        if (genre == null) return Genre.DRAMA;
        return switch (genre.toLowerCase()) {
            case "comedy" -> Genre.COMEDY;
            case "thriller" -> Genre.THRILLER;
            case "scifi", "sci-fi", "science fiction" -> Genre.SCIENCE_FICTION;
            case "crime" -> Genre.CRIME;
            default -> Genre.DRAMA;
        };
    }
}
