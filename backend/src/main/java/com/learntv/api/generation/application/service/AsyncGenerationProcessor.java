package com.learntv.api.generation.application.service;

import com.learntv.api.catalog.adapter.out.persistence.ShowJpaRepository;
import com.learntv.api.catalog.application.port.UserShowRepository;
import com.learntv.api.generation.application.port.in.GenerationCommand;
import com.learntv.api.generation.application.port.out.*;
import com.learntv.api.generation.domain.model.*;
import com.learntv.api.generation.domain.service.EpisodeLessonGenerator;
import com.learntv.api.learning.adapter.out.persistence.EpisodeJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Async processor for lesson generation.
 *
 * This is a separate service to ensure Spring's @Async proxy works correctly.
 * When @Async is called from within the same class, Spring's proxy is bypassed
 * and the method runs synchronously.
 */
@Service
public class AsyncGenerationProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncGenerationProcessor.class);

    private final JobProgressService jobProgressService;
    private final ScriptFetchService scriptFetchService;
    private final ContentExtractionPort contentExtractionPort;
    private final ExerciseGenerationPort exerciseGenerationPort;
    private final LessonPersistencePort lessonPersistencePort;
    private final EpisodeLessonGenerator episodeLessonGenerator;
    private final ShowMetadataPort showMetadataPort;
    private final ShowJpaRepository showJpaRepository;
    private final EpisodeJpaRepository episodeJpaRepository;
    private final UserShowRepository userShowRepository;

    public AsyncGenerationProcessor(
            JobProgressService jobProgressService,
            ScriptFetchService scriptFetchService,
            ContentExtractionPort contentExtractionPort,
            ExerciseGenerationPort exerciseGenerationPort,
            LessonPersistencePort lessonPersistencePort,
            EpisodeLessonGenerator episodeLessonGenerator,
            ShowMetadataPort showMetadataPort,
            ShowJpaRepository showJpaRepository,
            EpisodeJpaRepository episodeJpaRepository,
            UserShowRepository userShowRepository) {
        this.jobProgressService = jobProgressService;
        this.scriptFetchService = scriptFetchService;
        this.contentExtractionPort = contentExtractionPort;
        this.exerciseGenerationPort = exerciseGenerationPort;
        this.lessonPersistencePort = lessonPersistencePort;
        this.episodeLessonGenerator = episodeLessonGenerator;
        this.showMetadataPort = showMetadataPort;
        this.showJpaRepository = showJpaRepository;
        this.episodeJpaRepository = episodeJpaRepository;
        this.userShowRepository = userShowRepository;
    }

    /**
     * Process the generation asynchronously.
     * This method runs in a separate thread and updates progress at each step.
     */
    @Async
    public void processGeneration(UUID jobId, String imdbId, GenerationCommand command, UUID userId) {
        try {
            log.info("Starting async processing for job: {}", jobId);

            // Check if episode already exists — if so, simulate progress and grant access
            if (tryReuseExistingEpisode(jobId, command, userId)) {
                return;
            }

            // Mark as processing
            jobProgressService.updateProgress(jobId, GenerationProgressStep.FETCHING_SCRIPT);

            // Step 1: Fetch script
            String scriptText = scriptFetchService.fetchScript(
                    imdbId,
                    command.seasonNumber(),
                    command.episodeNumber()
            ).orElseThrow(() -> new RuntimeException(
                    "Script not found for " + imdbId + " S" + command.seasonNumber() +
                            "E" + command.episodeNumber()
            ));

            jobProgressService.updateProgress(jobId, GenerationProgressStep.PARSING_SCRIPT);
            log.info("Script fetched for job: {} ({} characters)", jobId, scriptText.length());

            // Step 2: Extract vocabulary
            jobProgressService.updateProgress(jobId, GenerationProgressStep.EXTRACTING_VOCABULARY);
            List<ExtractedVocabulary> vocabulary = contentExtractionPort.extractVocabulary(
                    scriptText,
                    command.genre()
            );
            log.info("Extracted {} vocabulary items for job: {}", vocabulary.size(), jobId);

            // Step 3: Extract grammar
            jobProgressService.updateProgress(jobId, GenerationProgressStep.EXTRACTING_GRAMMAR);
            List<ExtractedGrammar> grammar = contentExtractionPort.extractGrammar(scriptText);
            log.info("Extracted {} grammar points for job: {}", grammar.size(), jobId);

            // Step 4: Extract expressions
            jobProgressService.updateProgress(jobId, GenerationProgressStep.EXTRACTING_EXPRESSIONS);
            List<ExtractedExpression> expressions = contentExtractionPort.extractExpressions(scriptText);
            log.info("Extracted {} expressions for job: {}", expressions.size(), jobId);

            // Step 5: Generate exercises
            jobProgressService.updateProgress(jobId, GenerationProgressStep.GENERATING_EXERCISES);
            List<GeneratedExercise> exercises = exerciseGenerationPort.generateExercises(
                    vocabulary, grammar, expressions
            );
            log.info("Generated {} exercises for job: {}", exercises.size(), jobId);

            // Step 6: Compose lesson using domain service
            GeneratedLesson lesson = episodeLessonGenerator.generate(
                    vocabulary,
                    grammar,
                    expressions,
                    exercises
            );

            // Log quality metrics
            if (!episodeLessonGenerator.isHighQuality(lesson)) {
                log.warn("Lesson quality below optimal for job: {} - consider reviewing content", jobId);
            }

            int totalPoints = episodeLessonGenerator.calculateTotalPoints(lesson);
            log.info("Lesson composed for job: {} with {} total points", jobId, totalPoints);

            // Step 8: Persist lesson
            jobProgressService.updateProgress(jobId, GenerationProgressStep.SAVING);
            UUID episodeId = lessonPersistencePort.save(
                    lesson,
                    command.tmdbId(),
                    imdbId,
                    command.seasonNumber(),
                    command.episodeNumber(),
                    null,
                    command.genre(),
                    null,
                    userId
            );
            log.info("Lesson persisted for job: {} as episode: {}", jobId, episodeId);

            // Mark as completed
            jobProgressService.updateProgress(jobId, GenerationProgressStep.COMPLETED);
            jobProgressService.markCompleted(jobId, episodeId);

            log.info("Job completed successfully: {}", jobId);

        } catch (Exception e) {
            log.error("Job failed: {}", jobId, e);
            jobProgressService.markFailed(jobId, e.getMessage());
        }
    }

    /**
     * Check if the episode already exists in the DB. If so, simulate progress
     * and grant the user access instead of re-generating.
     *
     * @return true if existing episode was reused, false if real generation is needed
     */
    private boolean tryReuseExistingEpisode(UUID jobId, GenerationCommand command, UUID userId) {
        try {
            // Resolve show title from TMDB to find existing show by slug
            var showWithSeasons = showMetadataPort.getShowWithSeasons(command.tmdbId());
            if (showWithSeasons.isEmpty()) return false;

            String showTitle = showWithSeasons.get().title();
            String slug = showTitle.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");

            var existingShow = showJpaRepository.findBySlug(slug);
            if (existingShow.isEmpty()) return false;

            var existingEpisode = episodeJpaRepository.findByShowIdAndSeasonNumberAndEpisodeNumber(
                    existingShow.get().getId(), command.seasonNumber(), command.episodeNumber());
            if (existingEpisode.isEmpty()) return false;

            UUID episodeId = existingEpisode.get().getId();
            log.info("Episode already exists for job: {} — simulating progress and granting access", jobId);

            // Simulate progress with realistic per-step delays (ms)
            Map<GenerationProgressStep, int[]> stepDelays = Map.of(
                    GenerationProgressStep.FETCHING_SCRIPT, new int[]{2000, 4000},
                    GenerationProgressStep.PARSING_SCRIPT, new int[]{1500, 3000},
                    GenerationProgressStep.EXTRACTING_VOCABULARY, new int[]{5000, 9000},
                    GenerationProgressStep.EXTRACTING_GRAMMAR, new int[]{4000, 7000},
                    GenerationProgressStep.EXTRACTING_EXPRESSIONS, new int[]{4000, 7000},
                    GenerationProgressStep.GENERATING_EXERCISES, new int[]{5000, 9000},
                    GenerationProgressStep.SAVING, new int[]{1000, 2000}
            );
            for (GenerationProgressStep step : GenerationProgressStep.values()) {
                if (step == GenerationProgressStep.COMPLETED) break;
                jobProgressService.updateProgress(jobId, step);
                int[] range = stepDelays.getOrDefault(step, new int[]{1000, 2000});
                Thread.sleep(ThreadLocalRandom.current().nextInt(range[0], range[1]));
            }

            // Grant user access to the show
            userShowRepository.addUserShow(userId, existingShow.get().getId());

            // Mark job as completed with the existing episode ID
            jobProgressService.updateProgress(jobId, GenerationProgressStep.COMPLETED);
            jobProgressService.markCompleted(jobId, episodeId);

            log.info("Job completed (reused existing episode) for job: {} episode: {}", jobId, episodeId);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Failed to check for existing episode, proceeding with generation: {}", e.getMessage());
            return false;
        }
    }
}
