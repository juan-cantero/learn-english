package com.learntv.api.generation.application.service;

import com.learntv.api.learning.adapter.out.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to regenerate audio for existing vocabulary and expressions.
 * Use this when audio generation failed during initial lesson creation
 * or when audio needs to be updated.
 */
@Service
public class AudioRegenerationService {

    private static final Logger log = LoggerFactory.getLogger(AudioRegenerationService.class);
    private static final int MAX_CONCURRENT_GENERATIONS = 10;
    private static final int TIMEOUT_MINUTES = 30;

    private final AudioGenerationService audioGenerationService;
    private final VocabularyJpaRepository vocabularyRepository;
    private final ExpressionJpaRepository expressionRepository;
    private final ExerciseJpaRepository exerciseRepository;
    private final EpisodeJpaRepository episodeRepository;

    public AudioRegenerationService(
            AudioGenerationService audioGenerationService,
            VocabularyJpaRepository vocabularyRepository,
            ExpressionJpaRepository expressionRepository,
            ExerciseJpaRepository exerciseRepository,
            EpisodeJpaRepository episodeRepository) {
        this.audioGenerationService = audioGenerationService;
        this.vocabularyRepository = vocabularyRepository;
        this.expressionRepository = expressionRepository;
        this.exerciseRepository = exerciseRepository;
        this.episodeRepository = episodeRepository;
    }

    /**
     * Regenerate audio for all vocabulary and expressions in an episode.
     * Only processes items that don't already have an audio URL.
     *
     * @param episodeId The episode ID
     * @param forceRegenerate If true, regenerate all audio even if URLs exist
     * @return Result with counts of regenerated items
     */
    @Transactional
    public RegenerationResult regenerateAudioForEpisode(UUID episodeId, boolean forceRegenerate) {
        log.info("Starting audio regeneration for episode: {}, forceRegenerate: {}", episodeId, forceRegenerate);

        // Verify episode exists
        if (!episodeRepository.existsById(episodeId)) {
            throw new IllegalArgumentException("Episode not found: " + episodeId);
        }

        AtomicInteger vocabSuccess = new AtomicInteger(0);
        AtomicInteger vocabFailed = new AtomicInteger(0);
        AtomicInteger exprSuccess = new AtomicInteger(0);
        AtomicInteger exprFailed = new AtomicInteger(0);
        AtomicInteger exerciseSuccess = new AtomicInteger(0);
        AtomicInteger exerciseFailed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_GENERATIONS);

        try {
            // Regenerate vocabulary audio
            List<VocabularyJpaEntity> vocabulary = vocabularyRepository.findByEpisodeId(episodeId);
            log.info("Found {} vocabulary items for episode {}", vocabulary.size(), episodeId);

            for (VocabularyJpaEntity vocab : vocabulary) {
                if (!forceRegenerate && vocab.getAudioUrl() != null) {
                    log.debug("Skipping vocabulary with existing audio: {}", vocab.getTerm());
                    continue;
                }

                executor.submit(() -> {
                    try {
                        String audioUrl = generateAndUploadAudio(vocab.getTerm(), "vocab");
                        vocab.setAudioUrl(audioUrl);
                        vocabularyRepository.save(vocab);
                        vocabSuccess.incrementAndGet();
                        log.info("Generated audio for vocabulary: {} -> {}", vocab.getTerm(), audioUrl);
                    } catch (Exception e) {
                        vocabFailed.incrementAndGet();
                        log.warn("Failed to generate audio for vocabulary: {}. Error: {}",
                                vocab.getTerm(), e.getMessage());
                    }
                });
            }

            // Regenerate expression audio
            List<ExpressionJpaEntity> expressions = expressionRepository.findByEpisodeId(episodeId);
            log.info("Found {} expressions for episode {}", expressions.size(), episodeId);

            for (ExpressionJpaEntity expr : expressions) {
                if (!forceRegenerate && expr.getAudioUrl() != null) {
                    log.debug("Skipping expression with existing audio: {}", expr.getPhrase());
                    continue;
                }

                executor.submit(() -> {
                    try {
                        String audioUrl = generateAndUploadAudio(expr.getPhrase(), "expr");
                        expr.setAudioUrl(audioUrl);
                        expressionRepository.save(expr);
                        exprSuccess.incrementAndGet();
                        log.info("Generated audio for expression: {} -> {}", expr.getPhrase(), audioUrl);
                    } catch (Exception e) {
                        exprFailed.incrementAndGet();
                        log.warn("Failed to generate audio for expression: {}. Error: {}",
                                expr.getPhrase(), e.getMessage());
                    }
                });
            }

            // Regenerate listening exercise audio
            List<ExerciseJpaEntity> exercises = exerciseRepository.findByEpisodeId(episodeId);
            List<ExerciseJpaEntity> listeningExercises = exercises.stream()
                    .filter(e -> "LISTENING".equals(e.getType().name()))
                    .toList();
            log.info("Found {} listening exercises for episode {}", listeningExercises.size(), episodeId);

            for (ExerciseJpaEntity exercise : listeningExercises) {
                if (!forceRegenerate && exercise.getAudioUrl() != null) {
                    log.debug("Skipping listening exercise with existing audio: {}", exercise.getCorrectAnswer());
                    continue;
                }

                executor.submit(() -> {
                    try {
                        String word = exercise.getCorrectAnswer();
                        if (word != null && !word.isBlank()) {
                            String audioUrl = generateAndUploadAudio(word, "listening");
                            exercise.setAudioUrl(audioUrl);
                            exerciseRepository.save(exercise);
                            exerciseSuccess.incrementAndGet();
                            log.info("Generated audio for listening exercise: {} -> {}", word, audioUrl);
                        }
                    } catch (Exception e) {
                        exerciseFailed.incrementAndGet();
                        log.warn("Failed to generate audio for listening exercise: {}. Error: {}",
                                exercise.getCorrectAnswer(), e.getMessage());
                    }
                });
            }

            // Wait for all tasks to complete
            executor.shutdown();
            if (!executor.awaitTermination(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                log.warn("Audio regeneration timed out after {} minutes", TIMEOUT_MINUTES);
                executor.shutdownNow();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Audio regeneration interrupted", e);
            executor.shutdownNow();
        }

        RegenerationResult result = new RegenerationResult(
                vocabSuccess.get(),
                vocabFailed.get(),
                exprSuccess.get(),
                exprFailed.get(),
                exerciseSuccess.get(),
                exerciseFailed.get()
        );

        log.info("Audio regeneration completed for episode {}: {}", episodeId, result);
        return result;
    }

    /**
     * Regenerate audio for an episode by show slug and episode slug.
     */
    @Transactional
    public RegenerationResult regenerateAudioForEpisode(String showSlug, String episodeSlug, boolean forceRegenerate) {
        EpisodeJpaEntity episode = episodeRepository.findByShowSlugAndSlug(showSlug, episodeSlug)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episode not found: " + showSlug + "/" + episodeSlug));

        return regenerateAudioForEpisode(episode.getId(), forceRegenerate);
    }

    /**
     * Generate and upload audio for a text.
     */
    private String generateAndUploadAudio(String text, String prefix) {
        byte[] wav = audioGenerationService.generateWavForText(text);
        byte[] mp3 = audioGenerationService.convertWavToMp3(wav);
        String key = prefix + "/" + slugify(text) + ".mp3";
        return audioGenerationService.uploadAudio(key, mp3);
    }

    /**
     * Convert text to URL-safe slug.
     */
    private String slugify(String text) {
        String slug = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        // Truncate if too long
        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
        }
        return slug;
    }

    /**
     * Result of audio regeneration.
     */
    public record RegenerationResult(
            int vocabularySuccess,
            int vocabularyFailed,
            int expressionsSuccess,
            int expressionsFailed,
            int exercisesSuccess,
            int exercisesFailed
    ) {
        public int totalSuccess() {
            return vocabularySuccess + expressionsSuccess + exercisesSuccess;
        }

        public int totalFailed() {
            return vocabularyFailed + expressionsFailed + exercisesFailed;
        }
    }
}
