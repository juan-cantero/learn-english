package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.GenerationJob;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for managing async generation jobs.
 * This is the output port that the domain layer uses to persist job state.
 */
public interface GenerationJobRepository {

    /**
     * Create and save a new generation job.
     *
     * @param imdbId IMDB ID of the episode
     * @param seasonNumber Season number
     * @param episodeNumber Episode number
     * @return The created job with generated ID
     */
    GenerationJob createJob(String imdbId, int seasonNumber, int episodeNumber);

    /**
     * Save or update a generation job.
     *
     * @param job The job to save
     * @return The saved job
     */
    GenerationJob save(GenerationJob job);

    /**
     * Find a job by its ID.
     *
     * @param jobId The job ID
     * @return Optional containing the job if found
     */
    Optional<GenerationJob> findById(UUID jobId);

    /**
     * Update job progress.
     *
     * @param jobId Job ID
     * @param step Current step description
     * @param progress Progress percentage (0-100)
     */
    void updateProgress(UUID jobId, String step, int progress);

    /**
     * Mark a job as completed.
     *
     * @param jobId Job ID
     * @param episodeId ID of the generated episode
     */
    void markCompleted(UUID jobId, UUID episodeId);

    /**
     * Mark a job as failed.
     *
     * @param jobId Job ID
     * @param errorMessage Error message describing the failure
     */
    void markFailed(UUID jobId, String errorMessage);
}
