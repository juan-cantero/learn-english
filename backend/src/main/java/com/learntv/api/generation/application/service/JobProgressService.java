package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.out.GenerationJobRepository;
import com.learntv.api.generation.domain.model.GenerationJob;
import com.learntv.api.generation.domain.model.GenerationProgressStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for updating job progress in separate transactions.
 *
 * Uses REQUIRES_NEW propagation so each progress update is committed
 * immediately and visible to polling clients, regardless of the
 * parent transaction's state.
 */
@Service
public class JobProgressService {

    private static final Logger log = LoggerFactory.getLogger(JobProgressService.class);

    private final GenerationJobRepository jobRepository;

    public JobProgressService(GenerationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Update job progress in a NEW transaction.
     * This commits immediately, making progress visible to polling clients.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(UUID jobId, GenerationProgressStep step) {
        GenerationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));

        GenerationJob updated = job.updateProgress(step.getProgress(), step.getDescription());
        jobRepository.save(updated);

        log.debug("Job {} progress: {}% - {}", jobId, step.getProgress(), step.getDescription());
    }

    /**
     * Mark job as completed in a NEW transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(UUID jobId, UUID episodeId) {
        jobRepository.markCompleted(jobId, episodeId);
        log.info("Job {} marked as COMPLETED with episode {}", jobId, episodeId);
    }

    /**
     * Mark job as failed in a NEW transaction.
     * This ensures the failure is recorded even if the main transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID jobId, String errorMessage) {
        jobRepository.markFailed(jobId, errorMessage);
        log.error("Job {} marked as FAILED: {}", jobId, errorMessage);
    }
}
