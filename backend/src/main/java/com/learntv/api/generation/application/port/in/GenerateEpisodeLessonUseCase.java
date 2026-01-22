package com.learntv.api.generation.application.port.in;

import com.learntv.api.generation.domain.model.GenerationJob;

/**
 * Use case for starting the async generation of lesson content from an episode.
 *
 * This is the primary input port for the generation bounded context.
 * The implementation (Application Service) will:
 * 1. Create a GenerationJob to track progress
 * 2. Start async processing
 * 3. Return the job immediately so the caller can poll for status
 */
public interface GenerateEpisodeLessonUseCase {

    /**
     * Start generating lesson content for an episode.
     * This is an async operation - returns immediately with a job for tracking.
     *
     * @param command The generation parameters (TMDB ID, season, episode, genre)
     * @return The created GenerationJob in PENDING state (use its ID to poll status)
     */
    GenerationJob startGeneration(GenerationCommand command);
}
