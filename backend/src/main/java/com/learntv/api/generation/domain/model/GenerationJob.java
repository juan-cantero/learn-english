package com.learntv.api.generation.domain.model;

import java.time.Instant;
import java.util.UUID;

public record GenerationJob(
    UUID id,
    GenerationStatus status,
    int progress,
    String currentStep,
    String errorMessage,
    UUID episodeId,
    Instant createdAt
) {
    public static GenerationJob create() {
        return new GenerationJob(
            UUID.randomUUID(),
            GenerationStatus.PENDING,
            0,
            null,
            null,
            null,
            Instant.now()
        );
    }
}
