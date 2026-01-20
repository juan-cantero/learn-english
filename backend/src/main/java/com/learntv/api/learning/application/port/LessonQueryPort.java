package com.learntv.api.learning.application.port;

import com.learntv.api.learning.domain.model.Lesson;

import java.util.Optional;

/**
 * Query port for reading lesson data.
 * Separates read concerns from write concerns (CQRS-lite).
 * Implementations should optimize for read performance (single query, projections, etc.)
 */
public interface LessonQueryPort {

    /**
     * Load complete lesson with all content in a single optimized query.
     * Returns episode + vocabulary + grammar + expressions + exercises.
     */
    Optional<Lesson> loadFullLesson(String showSlug, String episodeSlug);
}
