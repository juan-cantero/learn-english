package com.learntv.api.generation.adapter.out.persistence;

import com.learntv.api.generation.application.port.out.GenerationJobRepository;
import com.learntv.api.generation.domain.model.GenerationJob;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementation of GenerationJobRepository port.
 * Handles persistence of async generation jobs.
 */
@Repository
@RequiredArgsConstructor
public class GenerationJobRepositoryAdapter implements GenerationJobRepository {

    private final GenerationJobJpaRepository jpaRepository;

    @Override
    public GenerationJob createJob(String imdbId, int seasonNumber, int episodeNumber) {
        UUID jobId = UUID.randomUUID();
        GenerationJobJpaEntity entity = GenerationJobJpaEntity.create(jobId, imdbId, seasonNumber, episodeNumber);
        GenerationJobJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public GenerationJob save(GenerationJob job) {
        return jpaRepository.findById(job.id())
                .map(entity -> {
                    // Update existing entity
                    entity.setStatus(job.status());
                    entity.setCurrentStep(job.currentStep());
                    entity.setProgress(job.progress());
                    entity.setErrorMessage(job.errorMessage());
                    entity.setResultEpisodeId(job.episodeId());
                    entity.setCompletedAt(job.completedAt());
                    return jpaRepository.save(entity);
                })
                .map(GenerationJobJpaEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + job.id()));
    }

    @Override
    public Optional<GenerationJob> findById(UUID jobId) {
        return jpaRepository.findById(jobId)
                .map(GenerationJobJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateProgress(UUID jobId, String step, int progress) {
        jpaRepository.findById(jobId).ifPresent(entity -> {
            entity.setCurrentStep(step);
            entity.setProgress(progress);
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void markCompleted(UUID jobId, UUID episodeId) {
        jpaRepository.findById(jobId).ifPresent(entity -> {
            entity.setStatus(com.learntv.api.generation.domain.model.GenerationStatus.COMPLETED);
            entity.setProgress(100);
            entity.setCurrentStep("Completed");
            entity.setResultEpisodeId(episodeId);
            entity.setCompletedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional
    public void markFailed(UUID jobId, String errorMessage) {
        jpaRepository.findById(jobId).ifPresent(entity -> {
            entity.setStatus(com.learntv.api.generation.domain.model.GenerationStatus.FAILED);
            entity.setErrorMessage(errorMessage);
            entity.setCompletedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }
}
