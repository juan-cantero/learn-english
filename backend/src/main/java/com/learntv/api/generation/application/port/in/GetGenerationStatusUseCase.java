package com.learntv.api.generation.application.port.in;

import com.learntv.api.generation.domain.model.GenerationJob;
import java.util.UUID;

public interface GetGenerationStatusUseCase {
    GenerationJob getStatus(UUID jobId);
}
