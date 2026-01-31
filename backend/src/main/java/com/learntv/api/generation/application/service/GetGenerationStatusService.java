package com.learntv.api.generation.application.service;

import com.learntv.api.generation.application.port.in.GetGenerationStatusUseCase;
import com.learntv.api.generation.application.port.out.GenerationJobRepository;
import com.learntv.api.generation.domain.model.GenerationJob;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use Case implementation for querying generation job status.
 */
@Service
public class GetGenerationStatusService implements GetGenerationStatusUseCase {

    private final GenerationJobRepository jobRepository;

    public GetGenerationStatusService(GenerationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public GenerationJob getStatus(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }
}
