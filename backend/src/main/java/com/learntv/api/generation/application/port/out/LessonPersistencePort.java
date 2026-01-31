package com.learntv.api.generation.application.port.out;

import com.learntv.api.generation.domain.model.GeneratedLesson;

import java.util.UUID;

/**
 * Port for persisting generated lessons to the database.
 */
public interface LessonPersistencePort {

    /**
     * Save a generated lesson to the database.
     *
     * @param lesson The generated lesson content
     * @param tmdbId TMDB show ID
     * @param imdbId IMDB episode ID
     * @param seasonNumber Season number
     * @param episodeNumber Episode number
     * @param episodeTitle Episode title (optional)
     * @param genre Show genre
     * @param imageUrl Show image URL (optional)
     * @return The ID of the created episode
     */
    UUID save(
            GeneratedLesson lesson,
            String tmdbId,
            String imdbId,
            int seasonNumber,
            int episodeNumber,
            String episodeTitle,
            String genre,
            String imageUrl
    );
}
