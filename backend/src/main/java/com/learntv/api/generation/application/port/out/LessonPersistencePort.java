package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.GeneratedLesson;

import java.util.UUID;

public interface LessonPersistencePort {
    UUID save(GeneratedLesson lesson);
}
