package com.learntv.api.generation.adapter.out.persistence;

import com.learntv.api.generation.domain.model.GenerationJob;
import com.learntv.api.generation.domain.model.GenerationStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for async generation jobs.
 * Maps to the generation_jobs table.
 */
@Entity
@Table(name = "generation_jobs")
public class GenerationJobJpaEntity {

    @Id
    private UUID id;

    @Column(name = "imdb_id", nullable = false, length = 20)
    private String imdbId;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(name = "episode_number", nullable = false)
    private int episodeNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GenerationStatus status;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(nullable = false)
    private int progress;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "result_episode_id")
    private UUID resultEpisodeId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected GenerationJobJpaEntity() {
    }

    /**
     * Maps a domain GenerationJob to JPA entity.
     */
    public static GenerationJobJpaEntity fromDomain(GenerationJob job) {
        GenerationJobJpaEntity entity = new GenerationJobJpaEntity();
        entity.id = job.id();
        entity.imdbId = null; // Will be set separately when creating jobs
        entity.seasonNumber = 0; // Will be set separately when creating jobs
        entity.episodeNumber = 0; // Will be set separately when creating jobs
        entity.status = job.status();
        entity.currentStep = job.currentStep();
        entity.progress = job.progress();
        entity.errorMessage = job.errorMessage();
        entity.resultEpisodeId = job.episodeId();
        entity.createdAt = job.createdAt();
        entity.completedAt = job.completedAt();
        return entity;
    }

    /**
     * Factory method to create a new job entity.
     */
    public static GenerationJobJpaEntity create(UUID id, String imdbId, int seasonNumber, int episodeNumber) {
        GenerationJobJpaEntity entity = new GenerationJobJpaEntity();
        entity.id = id;
        entity.imdbId = imdbId;
        entity.seasonNumber = seasonNumber;
        entity.episodeNumber = episodeNumber;
        entity.status = GenerationStatus.PENDING;
        entity.progress = 0;
        entity.createdAt = Instant.now();
        return entity;
    }

    /**
     * Converts this JPA entity to a domain model.
     */
    public GenerationJob toDomain() {
        return new GenerationJob(
                id,
                status,
                progress,
                currentStep,
                errorMessage,
                resultEpisodeId,
                createdAt,
                completedAt
        );
    }

    // Getters and setters for JPA

    public UUID getId() {
        return id;
    }

    public String getImdbId() {
        return imdbId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public UUID getResultEpisodeId() {
        return resultEpisodeId;
    }

    public void setResultEpisodeId(UUID resultEpisodeId) {
        this.resultEpisodeId = resultEpisodeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
