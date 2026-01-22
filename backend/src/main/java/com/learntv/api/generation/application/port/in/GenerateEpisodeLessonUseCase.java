package com.learntv.api.generation.application.port.in;

import java.util.UUID;

public interface GenerateEpisodeLessonUseCase {
    UUID startGeneration(GenerationCommand command);
}
