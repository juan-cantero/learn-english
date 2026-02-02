package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.AudioGenerationPort;
import com.learntv.api.generation.application.port.out.AudioStoragePort;
import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service that orchestrates audio generation for vocabulary items.
 * Generates TTS audio using Piper, converts to MP3, and uploads to R2 storage.
 */
@Service
public class AudioGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AudioGenerationService.class);
    private static final int MAX_CONCURRENT_GENERATIONS = 10;
    private static final int TIMEOUT_MINUTES = 15;

    private final AudioGenerationPort audioGeneration;
    private final AudioStoragePort audioStorage;

    public AudioGenerationService(
            AudioGenerationPort audioGeneration,
            AudioStoragePort audioStorage) {
        this.audioGeneration = audioGeneration;
        this.audioStorage = audioStorage;
    }

    /**
     * Generate audio for a list of vocabulary items.
     * Processes items in parallel with a maximum of 10 concurrent operations.
     * If audio generation fails for an item, logs a warning and sets audioUrl to null.
     *
     * @param vocabulary List of vocabulary items to generate audio for
     * @return List of vocabulary items with audio URLs populated (or null if failed)
     */
    public List<ExtractedVocabulary> generateAudioForVocabulary(
            List<ExtractedVocabulary> vocabulary) {

        if (vocabulary == null || vocabulary.isEmpty()) {
            log.info("No vocabulary items to generate audio for");
            return List.of();
        }

        log.info("Starting audio generation for {} vocabulary items", vocabulary.size());

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_GENERATIONS);

        try {
            List<ExtractedVocabulary> results = vocabulary.stream()
                    .map(vocab -> executor.submit(() -> generateAudioForSingleVocab(vocab)))
                    .collect(Collectors.toList())
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("Failed to retrieve audio generation result", e);
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());

            log.info("Audio generation completed: {}/{} successful",
                    results.stream().filter(v -> v.audioUrl() != null).count(),
                    vocabulary.size());

            return results;

        } finally {
            shutdownExecutor(executor);
        }
    }

    /**
     * Generate audio for a list of expression items.
     * Processes items in parallel with a maximum of 10 concurrent operations.
     * If audio generation fails for an item, logs a warning and sets audioUrl to null.
     *
     * @param expressions List of expressions to generate audio for
     * @return List of expressions with audio URLs populated (or null if failed)
     */
    public List<ExtractedExpression> generateAudioForExpressions(
            List<ExtractedExpression> expressions) {

        if (expressions == null || expressions.isEmpty()) {
            log.info("No expressions to generate audio for");
            return List.of();
        }

        log.info("Starting audio generation for {} expressions", expressions.size());

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_GENERATIONS);

        try {
            List<ExtractedExpression> results = expressions.stream()
                    .map(expr -> executor.submit(() -> generateAudioForSingleExpression(expr)))
                    .collect(Collectors.toList())
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.error("Failed to retrieve audio generation result for expression", e);
                            return null;
                        }
                    })
                    .filter(result -> result != null)
                    .collect(Collectors.toList());

            log.info("Expression audio generation completed: {}/{} successful",
                    results.stream().filter(e -> e.audioUrl() != null).count(),
                    expressions.size());

            return results;

        } finally {
            shutdownExecutor(executor);
        }
    }

    /**
     * Generate audio for a single expression.
     * If generation fails, logs a warning and returns the expression with null audioUrl.
     */
    private ExtractedExpression generateAudioForSingleExpression(ExtractedExpression expr) {
        try {
            log.debug("Generating audio for expression: {}", expr.phrase());

            // Step 1: Generate WAV audio
            byte[] wav = audioGeneration.generateWav(expr.phrase());

            // Step 2: Convert to MP3
            byte[] mp3 = audioGeneration.convertToMp3(wav);

            // Step 3: Generate storage key
            String key = generateExpressionStorageKey(expr.phrase());

            // Step 4: Upload to R2 storage
            String url = audioStorage.upload(key, mp3, "audio/mpeg");

            log.info("Successfully generated audio for expression: {} -> {}", expr.phrase(), url);

            // Return new expression record with audio URL
            return expr.withAudioUrl(url);

        } catch (Exception e) {
            log.warn("Audio generation failed for expression: {}. Error: {}",
                    expr.phrase(), e.getMessage());

            // Return expression with null audioUrl on failure
            return new ExtractedExpression(
                    expr.phrase(),
                    expr.meaning(),
                    expr.context(),
                    expr.usageNote(),
                    null
            );
        }
    }

    /**
     * Generate a storage key for expression audio files.
     * Format: expr/{slugified-phrase}.mp3
     */
    private String generateExpressionStorageKey(String phrase) {
        String slug = slugify(phrase);
        // Truncate if too long (max 100 chars for slug)
        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
        }
        return "expr/" + slug + ".mp3";
    }

    /**
     * Generate audio for a single vocabulary item.
     * If generation fails, logs a warning and returns the vocabulary with null audioUrl.
     */
    private ExtractedVocabulary generateAudioForSingleVocab(ExtractedVocabulary vocab) {
        try {
            log.debug("Generating audio for term: {}", vocab.term());

            // Step 1: Generate WAV audio
            byte[] wav = audioGeneration.generateWav(vocab.term());

            // Step 2: Convert to MP3
            byte[] mp3 = audioGeneration.convertToMp3(wav);

            // Step 3: Generate storage key
            String key = generateStorageKey(vocab.term());

            // Step 4: Upload to R2 storage
            String url = audioStorage.upload(key, mp3, "audio/mpeg");

            log.info("Successfully generated audio for term: {} -> {}", vocab.term(), url);

            // Return new vocabulary record with audio URL
            return new ExtractedVocabulary(
                    vocab.term(),
                    vocab.definition(),
                    vocab.phonetic(),
                    vocab.category(),
                    vocab.exampleSentence(),
                    url
            );

        } catch (Exception e) {
            log.warn("Audio generation failed for term: {}. Error: {}",
                    vocab.term(), e.getMessage());

            // Return vocabulary with null audioUrl on failure
            return new ExtractedVocabulary(
                    vocab.term(),
                    vocab.definition(),
                    vocab.phonetic(),
                    vocab.category(),
                    vocab.exampleSentence(),
                    null
            );
        }
    }

    /**
     * Generate a storage key for vocabulary audio files.
     * Format: vocab/{slugified-term}.mp3
     */
    private String generateStorageKey(String term) {
        String slug = slugify(term);
        return "vocab/" + slug + ".mp3";
    }

    /**
     * Convert a term to a URL-safe slug.
     * Removes special characters, converts to lowercase, and replaces spaces with hyphens.
     */
    private String slugify(String term) {
        return term.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Safely shutdown the executor service.
     */
    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                log.warn("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Executor shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
