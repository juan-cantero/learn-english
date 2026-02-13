package com.learntv.api.generation.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GenerationJobTest {

    @Test
    void create_shouldCreateJobInPendingState() {
        // When
        GenerationJob job = GenerationJob.create();

        // Then
        assertNotNull(job.id());
        assertEquals(GenerationStatus.PENDING, job.status());
        assertEquals(0, job.progress());
        assertNull(job.currentStep());
        assertNull(job.errorMessage());
        assertNull(job.episodeId());
        assertNotNull(job.createdAt());
        assertNull(job.completedAt());
    }

    @Test
    void markProcessing_shouldTransitionToProcessingState() {
        // Given
        GenerationJob job = GenerationJob.create();

        // When
        GenerationJob processing = job.markProcessing("Starting...");

        // Then
        assertEquals(job.id(), processing.id());
        assertEquals(GenerationStatus.PROCESSING, processing.status());
        assertEquals(0, processing.progress());
        assertEquals("Starting...", processing.currentStep());
        assertNull(processing.errorMessage());
        assertNull(processing.episodeId());
    }

    @Test
    void updateProgress_shouldUpdateProgressAndStep() {
        // Given
        GenerationJob job = GenerationJob.create().markProcessing("Starting...");

        // When
        GenerationJob updated = job.updateProgress(40, "Extracting vocabulary...");

        // Then
        assertEquals(GenerationStatus.PROCESSING, updated.status());
        assertEquals(40, updated.progress());
        assertEquals("Extracting vocabulary...", updated.currentStep());
    }

    @Test
    void updateProgress_shouldThrowExceptionForInvalidProgress() {
        // Given
        GenerationJob job = GenerationJob.create();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> job.updateProgress(-1, "Invalid"));
        assertThrows(IllegalArgumentException.class, () -> job.updateProgress(101, "Invalid"));
    }

    @Test
    void markCompleted_shouldTransitionToCompletedState() {
        // Given
        GenerationJob job = GenerationJob.create();
        UUID episodeId = UUID.randomUUID();

        // When
        GenerationJob completed = job.markCompleted(episodeId);

        // Then
        assertEquals(GenerationStatus.COMPLETED, completed.status());
        assertEquals(100, completed.progress());
        assertEquals("Completed", completed.currentStep());
        assertEquals(episodeId, completed.episodeId());
        assertNull(completed.errorMessage());
        assertNotNull(completed.completedAt());
    }

    @Test
    void markCompleted_shouldThrowExceptionForNullEpisodeId() {
        // Given
        GenerationJob job = GenerationJob.create();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> job.markCompleted(null));
    }

    @Test
    void markFailed_shouldTransitionToFailedState() {
        // Given
        GenerationJob job = GenerationJob.create().markProcessing("Starting...");
        String errorMessage = "Failed to fetch script";

        // When
        GenerationJob failed = job.markFailed(errorMessage);

        // Then
        assertEquals(GenerationStatus.FAILED, failed.status());
        assertEquals(errorMessage, failed.errorMessage());
        assertNull(failed.episodeId());
        assertNotNull(failed.completedAt());
    }

    @Test
    void isTerminal_shouldReturnTrueForCompletedOrFailed() {
        // Given
        GenerationJob pending = GenerationJob.create();
        GenerationJob processing = pending.markProcessing("Test");
        GenerationJob completed = pending.markCompleted(UUID.randomUUID());
        GenerationJob failed = pending.markFailed("Error");

        // Then
        assertFalse(pending.isTerminal());
        assertFalse(processing.isTerminal());
        assertTrue(completed.isTerminal());
        assertTrue(failed.isTerminal());
    }

    @Test
    void isSuccessful_shouldReturnTrueOnlyForCompleted() {
        // Given
        GenerationJob pending = GenerationJob.create();
        GenerationJob completed = pending.markCompleted(UUID.randomUUID());
        GenerationJob failed = pending.markFailed("Error");

        // Then
        assertFalse(pending.isSuccessful());
        assertTrue(completed.isSuccessful());
        assertFalse(failed.isSuccessful());
    }

    @Test
    void generationProgressStep_shouldHaveCorrectProgressValues() {
        // Verify progress steps are defined correctly
        assertEquals(10, GenerationProgressStep.FETCHING_SCRIPT.getProgress());
        assertEquals(20, GenerationProgressStep.PARSING_SCRIPT.getProgress());
        assertEquals(40, GenerationProgressStep.EXTRACTING_VOCABULARY.getProgress());
        assertEquals(55, GenerationProgressStep.EXTRACTING_GRAMMAR.getProgress());
        assertEquals(70, GenerationProgressStep.EXTRACTING_EXPRESSIONS.getProgress());
        assertEquals(85, GenerationProgressStep.GENERATING_EXERCISES.getProgress());
        assertEquals(95, GenerationProgressStep.SAVING.getProgress());
        assertEquals(100, GenerationProgressStep.COMPLETED.getProgress());
    }

    @Test
    void generationProgressStep_shouldHaveDescriptions() {
        // Verify all steps have descriptions
        for (GenerationProgressStep step : GenerationProgressStep.values()) {
            assertNotNull(step.getDescription());
            assertFalse(step.getDescription().isEmpty());
        }
    }
}
