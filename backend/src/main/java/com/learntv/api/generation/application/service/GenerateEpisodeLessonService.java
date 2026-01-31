package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.in.GenerateEpisodeLessonUseCase;
import com.learntv.api.generation.application.port.in.GenerationCommand;
import com.learntv.api.generation.application.port.out.*;
import com.learntv.api.generation.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use Case implementation for generating episode lessons.
 *
 * RESPONSIBILITIES:
 * - Create and track generation jobs
 * - Delegate async processing to AsyncGenerationProcessor
 *
 * The actual generation work is done by AsyncGenerationProcessor
 * to ensure Spring's @Async proxy works correctly.
 */
@Service
public class GenerateEpisodeLessonService implements GenerateEpisodeLessonUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateEpisodeLessonService.class);

    private final GenerationJobRepository jobRepository;
    private final ShowMetadataPort showMetadataPort;
    private final AsyncGenerationProcessor asyncProcessor;

    public GenerateEpisodeLessonService(
            GenerationJobRepository jobRepository,
            ShowMetadataPort showMetadataPort,
            AsyncGenerationProcessor asyncProcessor) {
        this.jobRepository = jobRepository;
        this.showMetadataPort = showMetadataPort;
        this.asyncProcessor = asyncProcessor;
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

        // Start async processing in separate bean (so @Async works)
        asyncProcessor.processGeneration(job.id(), imdbId, command);

        return job;
    }
}
