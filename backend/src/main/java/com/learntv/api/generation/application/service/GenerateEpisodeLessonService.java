package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.in.GenerateEpisodeLessonUseCase;
import com.learntv.api.generation.application.port.in.GenerationCommand;
import com.learntv.api.generation.application.port.out.*;
import com.learntv.api.generation.domain.model.*;
import com.learntv.api.generation.domain.service.EpisodeLessonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use Case implementation for generating episode lessons.
 *
 * RESPONSIBILITIES:
 * - Create and track generation jobs
 * - Coordinate async processing
 * - Update progress at each step
 * - Handle errors and update job status
 * - Delegate business logic to domain service
 * - Persist the final result
 *
 * DOES NOT:
 * - Contain business logic (that's in EpisodeLessonGenerator)
 * - Know the pedagogical order (domain service handles that)
 * - Make decisions about content quality (domain service validates)
 */
@Service
public class GenerateEpisodeLessonService implements GenerateEpisodeLessonUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateEpisodeLessonService.class);

    private final GenerationJobRepository jobRepository;
    private final ScriptFetchService scriptFetchService;
    private final ShowMetadataPort showMetadataPort;
    private final ContentExtractionPort contentExtractionPort;
    private final ExerciseGenerationPort exerciseGenerationPort;
    private final AudioGenerationService audioGenerationService;
    private final LessonPersistencePort lessonPersistencePort;
    private final EpisodeLessonGenerator episodeLessonGenerator;

    public GenerateEpisodeLessonService(
            GenerationJobRepository jobRepository,
            ScriptFetchService scriptFetchService,
            ShowMetadataPort showMetadataPort,
            ContentExtractionPort contentExtractionPort,
            ExerciseGenerationPort exerciseGenerationPort,
            AudioGenerationService audioGenerationService,
            LessonPersistencePort lessonPersistencePort,
            EpisodeLessonGenerator episodeLessonGenerator) {
        this.jobRepository = jobRepository;
        this.scriptFetchService = scriptFetchService;
        this.showMetadataPort = showMetadataPort;
        this.contentExtractionPort = contentExtractionPort;
        this.exerciseGenerationPort = exerciseGenerationPort;
        this.audioGenerationService = audioGenerationService;
        this.lessonPersistencePort = lessonPersistencePort;
        this.episodeLessonGenerator = episodeLessonGenerator;
    }

    @Override
    public GenerationJob startGeneration(GenerationCommand command) {
        log.info("Starting lesson generation for TMDB ID: {}, S{}E{}",
                command.tmdbId(), command.seasonNumber(), command.episodeNumber());

        // Get IMDB ID from TMDB
        String imdbId = showMetadataPort.getImdbId(
                command.tmdbId(),
                command.seasonNumber(),
                command.episodeNumber()
        );

        if (imdbId == null) {
            throw new IllegalArgumentException(
                    "Could not find IMDB ID for TMDB ID: " + command.tmdbId());
        }

        // Create job and return immediately
        GenerationJob job = jobRepository.createJob(
                imdbId,
                command.seasonNumber(),
                command.episodeNumber()
        );

        log.info("Created generation job: {} for IMDB ID: {}", job.id(), imdbId);

        // Start async processing
        processGenerationAsync(job.id(), imdbId, command);

        return job;
    }

    /**
     * Process the generation asynchronously.
     * This method runs in a separate thread and updates progress at each step.
     */
    @Async
    @Transactional
    public void processGenerationAsync(UUID jobId, String imdbId, GenerationCommand command) {
        try {
            log.info("Starting async processing for job: {}", jobId);

            // Mark as processing
            updateProgress(jobId, GenerationProgressStep.FETCHING_SCRIPT);

            // Step 1: Fetch script
            String scriptText = scriptFetchService.fetchScript(
                    imdbId,
                    command.seasonNumber(),
                    command.episodeNumber()
            ).orElseThrow(() -> new RuntimeException(
                    "Script not found for " + imdbId + " S" + command.seasonNumber() +
                            "E" + command.episodeNumber()
            ));

            updateProgress(jobId, GenerationProgressStep.PARSING_SCRIPT);
            log.info("Script fetched for job: {} ({} characters)", jobId, scriptText.length());

            // Step 2: Extract vocabulary
            updateProgress(jobId, GenerationProgressStep.EXTRACTING_VOCABULARY);
            List<ExtractedVocabulary> vocabulary = contentExtractionPort.extractVocabulary(
                    scriptText,
                    command.genre()
            );
            log.info("Extracted {} vocabulary items for job: {}", vocabulary.size(), jobId);

            // Step 3: Extract grammar
            updateProgress(jobId, GenerationProgressStep.EXTRACTING_GRAMMAR);
            List<ExtractedGrammar> grammar = contentExtractionPort.extractGrammar(scriptText);
            log.info("Extracted {} grammar points for job: {}", grammar.size(), jobId);

            // Step 4: Extract expressions
            updateProgress(jobId, GenerationProgressStep.EXTRACTING_EXPRESSIONS);
            List<ExtractedExpression> expressions = contentExtractionPort.extractExpressions(scriptText);
            log.info("Extracted {} expressions for job: {}", expressions.size(), jobId);

            // Step 5: Generate exercises
            updateProgress(jobId, GenerationProgressStep.GENERATING_EXERCISES);
            List<GeneratedExercise> exercises = exerciseGenerationPort.generateExercises(
                    vocabulary, grammar, expressions
            );
            log.info("Generated {} exercises for job: {}", exercises.size(), jobId);

            // Step 6: Generate audio for vocabulary
            updateProgress(jobId, GenerationProgressStep.GENERATING_AUDIO);
            List<ExtractedVocabulary> vocabularyWithAudio =
                    audioGenerationService.generateAudioForVocabulary(vocabulary);
            log.info("Generated audio for job: {}", jobId);

            // Step 7: Compose lesson using domain service
            GeneratedLesson lesson = episodeLessonGenerator.generate(
                    vocabularyWithAudio,
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
            updateProgress(jobId, GenerationProgressStep.SAVING);
            UUID episodeId = lessonPersistencePort.save(
                    lesson,
                    command.tmdbId(),
                    imdbId,
                    command.seasonNumber(),
                    command.episodeNumber(),
                    null, // episodeTitle - could be fetched from TMDB in the future
                    command.genre(),
                    null  // imageUrl - could be fetched from TMDB in the future
            );
            log.info("Lesson persisted for job: {} as episode: {}", jobId, episodeId);

            // Mark as completed
            updateProgress(jobId, GenerationProgressStep.COMPLETED);
            jobRepository.markCompleted(jobId, episodeId);

            log.info("Job completed successfully: {}", jobId);

        } catch (Exception e) {
            log.error("Job failed: {}", jobId, e);
            jobRepository.markFailed(jobId, e.getMessage());
        }
    }

    /**
     * Update job progress using the progress step enum.
     */
    private void updateProgress(UUID jobId, GenerationProgressStep step) {
        GenerationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        GenerationJob updated = job.updateProgress(step.getProgress(), step.getDescription());
        jobRepository.save(updated);

        log.debug("Job {} progress: {}% - {}", jobId, step.getProgress(), step.getDescription());
    }
}
