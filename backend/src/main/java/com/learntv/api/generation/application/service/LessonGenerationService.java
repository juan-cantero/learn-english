package com.learntv.api.generation.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntv.api.catalog.adapter.out.persistence.ShowJpaEntity;
import com.learntv.api.catalog.adapter.out.persistence.ShowJpaRepository;
import com.learntv.api.catalog.domain.model.*;
import com.learntv.api.generation.application.port.out.ContentExtractionPort;
import com.learntv.api.generation.application.port.out.ExerciseGenerationPort;
import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedExercise;
import com.learntv.api.learning.adapter.out.persistence.*;
import com.learntv.api.learning.domain.model.ExerciseType;
import com.learntv.api.learning.domain.model.VocabularyCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that orchestrates content generation and persistence.
 * Generates a complete lesson from a TV show episode and saves it to the database.
 */
@Service
public class LessonGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LessonGenerationService.class);

    private final ScriptFetchService scriptFetchService;
    private final ContentExtractionPort contentExtractionPort;
    private final ExerciseGenerationPort exerciseGenerationPort;
    private final AudioGenerationService audioGenerationService;
    private final ShowJpaRepository showRepository;
    private final EpisodeJpaRepository episodeRepository;
    private final VocabularyJpaRepository vocabularyRepository;
    private final GrammarPointJpaRepository grammarRepository;
    private final ExpressionJpaRepository expressionRepository;
    private final ExerciseJpaRepository exerciseRepository;
    private final ObjectMapper objectMapper;

    public LessonGenerationService(
            ScriptFetchService scriptFetchService,
            ContentExtractionPort contentExtractionPort,
            ExerciseGenerationPort exerciseGenerationPort,
            AudioGenerationService audioGenerationService,
            ShowJpaRepository showRepository,
            EpisodeJpaRepository episodeRepository,
            VocabularyJpaRepository vocabularyRepository,
            GrammarPointJpaRepository grammarRepository,
            ExpressionJpaRepository expressionRepository,
            ExerciseJpaRepository exerciseRepository,
            ObjectMapper objectMapper) {
        this.scriptFetchService = scriptFetchService;
        this.contentExtractionPort = contentExtractionPort;
        this.exerciseGenerationPort = exerciseGenerationPort;
        this.audioGenerationService = audioGenerationService;
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.grammarRepository = grammarRepository;
        this.expressionRepository = expressionRepository;
        this.exerciseRepository = exerciseRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate and save a complete lesson for an episode.
     */
    @Transactional
    public GeneratedLessonResult generateAndSaveLesson(LessonGenerationRequest request) {
        log.info("Generating lesson for {} S{}E{}", request.showTitle(), request.season(), request.episode());

        // 1. Find or create show
        ShowJpaEntity show = findOrCreateShow(request);
        log.info("Using show: {} ({})", show.getTitle(), show.getId());

        // 2. Check if episode already exists
        Optional<EpisodeJpaEntity> existingEpisode = episodeRepository
                .findByShowIdAndSeasonNumberAndEpisodeNumber(show.getId(), request.season(), request.episode());
        
        if (existingEpisode.isPresent()) {
            log.warn("Episode already exists: {} S{}E{}", request.showTitle(), request.season(), request.episode());
            return new GeneratedLessonResult(
                    existingEpisode.get().getId(),
                    show.getSlug(),
                    existingEpisode.get().getSlug(),
                    "Episode already exists",
                    0, 0, 0, 0
            );
        }

        // 3. Fetch script
        String script = scriptFetchService.fetchScript(request.imdbId(), request.season(), request.episode())
                .orElseThrow(() -> new RuntimeException("Could not fetch script for " + request.imdbId()));

        // 4. Create episode
        EpisodeJpaEntity episode = EpisodeJpaEntity.create(
                show.getId(),
                show.getSlug(),
                request.season(),
                request.episode(),
                request.episodeTitle() != null ? request.episodeTitle() : "Episode " + request.episode(),
                "Generated lesson for " + request.showTitle()
        );
        episodeRepository.save(episode);
        log.info("Created episode: {}", episode.getSlug());

        // 5. Extract vocabulary and generate audio
        List<ExtractedVocabulary> extractedVocab = contentExtractionPort.extractVocabulary(script, request.genre());
        List<ExtractedVocabulary> vocabWithAudio = audioGenerationService.generateAudioForVocabulary(extractedVocab);
        for (ExtractedVocabulary v : vocabWithAudio) {
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
        log.info("Saved {} vocabulary items", vocabWithAudio.size());

        // 6. Extract and save grammar points
        List<ExtractedGrammar> extractedGrammar = contentExtractionPort.extractGrammar(script);
        for (ExtractedGrammar g : extractedGrammar) {
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
        log.info("Saved {} grammar points", extractedGrammar.size());

        // 7. Extract expressions and generate audio
        List<ExtractedExpression> extractedExpressions = contentExtractionPort.extractExpressions(script);
        List<ExtractedExpression> expressionsWithAudio = audioGenerationService.generateAudioForExpressions(extractedExpressions);
        for (ExtractedExpression e : expressionsWithAudio) {
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
        log.info("Saved {} expressions", expressionsWithAudio.size());

        // 8. Generate and save exercises (with audio for LISTENING type)
        List<GeneratedExercise> generatedExercises = exerciseGenerationPort.generateExercises(
                vocabWithAudio, extractedGrammar, expressionsWithAudio);
        for (GeneratedExercise ex : generatedExercises) {
            String optionsJson = null;
            if (ex.options() != null) {
                try {
                    optionsJson = objectMapper.writeValueAsString(ex.options());
                } catch (JsonProcessingException e) {
                    optionsJson = String.join(",", ex.options());
                }
            }

            // Generate audio for LISTENING exercises
            String audioUrl = null;
            if ("LISTENING".equals(ex.type()) && ex.correctAnswer() != null) {
                audioUrl = generateAudioForListeningExercise(ex.correctAnswer());
            }

            ExerciseJpaEntity exercise = ExerciseJpaEntity.create(
                    episode.getId(),
                    ExerciseType.valueOf(ex.type()),
                    ex.question(),
                    ex.correctAnswer(),
                    optionsJson,
                    ex.points(),
                    audioUrl
            );
            exerciseRepository.save(exercise);
        }
        log.info("Saved {} exercises", generatedExercises.size());

        return new GeneratedLessonResult(
                episode.getId(),
                show.getSlug(),
                episode.getSlug(),
                "Lesson generated successfully",
                vocabWithAudio.size(),
                extractedGrammar.size(),
                expressionsWithAudio.size(),
                generatedExercises.size()
        );
    }

    private ShowJpaEntity findOrCreateShow(LessonGenerationRequest request) {
        String slug = generateSlug(request.showTitle());
        
        return showRepository.findBySlug(slug)
                .orElseGet(() -> {
                    log.info("Creating new show: {}", request.showTitle());
                    Show show = Show.builder()
                            .id(ShowId.of(UUID.randomUUID()))
                            .title(request.showTitle())
                            .slug(slug)
                            .description("Generated show for " + request.showTitle())
                            .genre(mapGenre(request.genre()))
                            .accent(AccentType.AMERICAN)
                            .difficulty(DifficultyLevel.INTERMEDIATE)
                            .imageUrl(request.imageUrl())
                            .totalSeasons(1)
                            .totalEpisodes(1)
                            .build();
                    return showRepository.save(ShowJpaEntity.fromDomain(show));
                });
    }

    /**
     * Generate audio for a listening exercise word.
     * Returns the audio URL or null if generation fails.
     */
    private String generateAudioForListeningExercise(String word) {
        try {
            byte[] wav = audioGenerationService.generateWavForText(word);
            byte[] mp3 = audioGenerationService.convertWavToMp3(wav);
            String key = "listening/" + slugify(word) + ".mp3";
            return audioGenerationService.uploadAudio(key, mp3);
        } catch (Exception e) {
            log.warn("Failed to generate audio for listening exercise: {}. Error: {}", word, e.getMessage());
            return null;
        }
    }

    private String slugify(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
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

    public record LessonGenerationRequest(
            String imdbId,
            int season,
            int episode,
            String showTitle,
            String episodeTitle,
            String genre,
            String imageUrl
    ) {}

    public record GeneratedLessonResult(
            UUID episodeId,
            String showSlug,
            String episodeSlug,
            String message,
            int vocabularyCount,
            int grammarCount,
            int expressionsCount,
            int exercisesCount
    ) {}
}
