package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.domain.model.GenerationJob;
import com.learntv.api.generation.domain.model.GenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response DTO for generation job status queries.
 */
@Schema(description = "Status of an async lesson generation job")
public record JobStatusResponse(
        @Schema(description = "Unique job identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID jobId,

        @Schema(description = "Current job status", example = "PROCESSING")
        GenerationStatus status,

        @Schema(description = "Progress percentage (0-100)", example = "45")
        int progress,

        @Schema(description = "Current step description", example = "Extracting vocabulary...")
        String currentStep,

        @Schema(description = "Error message if job failed", nullable = true)
        String errorMessage,

        @Schema(description = "Generated episode ID if job completed", nullable = true)
        UUID episodeId
) {
    /**
     * Creates a response DTO from a domain GenerationJob.
     */
    public static JobStatusResponse fromDomain(GenerationJob job) {
        return new JobStatusResponse(
                job.id(),
                job.status(),
                job.progress(),
                job.currentStep(),
                job.errorMessage(),
                job.episodeId()
        );
    }
}
