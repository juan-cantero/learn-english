package com.learntv.api.generation.domain.service;

import com.learntv.api.generation.domain.model.GeneratedLesson;
import com.learntv.api.generation.domain.model.Script;

/**
 * Domain Service that encapsulates the pedagogical logic for generating lessons.
 *
 * HEXAGONAL ARCHITECTURE NOTE:
 * This is a PURE domain service. It must NOT:
 * - Inject or call any ports (ShowMetadataPort, ContentExtractionPort, etc.)
 * - Make external API calls
 * - Persist anything
 * - Have framework annotations (@Service, @Component, etc.)
 *
 * The orchestration of external services happens in the Application Layer
 * (GenerateEpisodeLessonService). This domain service receives already-fetched
 * data and applies business rules to transform it.
 *
 * This service knows:
 * - The pedagogical order of content extraction (vocab → grammar → expressions → exercises)
 * - Business rules (min/max items, difficulty balancing, etc.)
 * - How to compose a GeneratedLesson from extracted content
 *
 * Dependencies will be injected as FUNCTION PARAMETERS (data), not as constructor
 * dependencies (ports). The Application Service calls the ports and passes the
 * results here.
 */
public class EpisodeLessonGenerator {

    /**
     * Generate a complete lesson from a script.
     *
     * In the full implementation, this will receive extracted content from
     * the Application Service and compose the final lesson with business rules.
     *
     * @param script The parsed script text from subtitles
     * @param genre The show's genre (affects vocabulary categorization)
     * @return A complete lesson with vocabulary, grammar, expressions, and exercises
     */
    public GeneratedLesson generate(Script script, String genre) {
        // Will be implemented in task 5.2
        // This method will receive already-extracted content and apply business rules
        throw new UnsupportedOperationException(
            "Will be implemented in task 5.2 - " +
            "This domain service will receive data from the Application Service, " +
            "not call ports directly."
        );
    }
}
