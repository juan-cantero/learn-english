package com.learntv.api.generation.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an async job for generating episode lesson content.
 * This is an immutable domain entity with state transition methods
 * that return new instances (following DDD patterns).
 */
public record GenerationJob(
    UUID id,
    GenerationStatus status,
    int progress,
    String currentStep,
    String errorMessage,
    UUID episodeId,
    Instant createdAt,
    Instant completedAt
) {

    /**
     * Factory method to create a new job in PENDING state.
     */
    public static GenerationJob create() {
        return new GenerationJob(
            UUID.randomUUID(),
            GenerationStatus.PENDING,
            0,
            null,
            null,
            null,
            Instant.now(),
            null
        );
    }

    /**
     * Transition to PROCESSING state.
     */
    public GenerationJob markProcessing(String step) {
        return new GenerationJob(
            id,
            GenerationStatus.PROCESSING,
            0,
            step,
            null,
            null,
            createdAt,
            null
        );
    }

    /**
     * Update progress within PROCESSING state.
     * @param progress Progress percentage (0-100)
     * @param step Current step description for user feedback
     */
    public GenerationJob updateProgress(int progress, String step) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        return new GenerationJob(
            id,
            GenerationStatus.PROCESSING,
            progress,
            step,
            null,
            null,
            createdAt,
            null
        );
    }

    /**
     * Transition to COMPLETED state with the generated episode ID.
     */
    public GenerationJob markCompleted(UUID generatedEpisodeId) {
        if (generatedEpisodeId == null) {
            throw new IllegalArgumentException("Episode ID is required for completion");
        }
        return new GenerationJob(
            id,
            GenerationStatus.COMPLETED,
            100,
            "Completed",
            null,
            generatedEpisodeId,
            createdAt,
            Instant.now()
        );
    }

    /**
     * Transition to FAILED state with error details.
     */
    public GenerationJob markFailed(String reason) {
        return new GenerationJob(
            id,
            GenerationStatus.FAILED,
            progress,
            currentStep,
            reason,
            null,
            createdAt,
            Instant.now()
        );
    }

    /**
     * Check if the job is in a terminal state (completed or failed).
     */
    public boolean isTerminal() {
        return status == GenerationStatus.COMPLETED || status == GenerationStatus.FAILED;
    }

    /**
     * Check if the job completed successfully.
     */
    public boolean isSuccessful() {
        return status == GenerationStatus.COMPLETED;
    }
}
