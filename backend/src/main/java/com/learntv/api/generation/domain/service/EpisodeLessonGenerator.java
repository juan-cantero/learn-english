package com.learntv.api.generation.domain.service;

import com.learntv.api.generation.domain.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain Service that encapsulates the pedagogical logic for generating lessons.
 *
 * HEXAGONAL ARCHITECTURE:
 * This is a PURE domain service - it contains ONLY business logic.
 * - NO framework annotations (@Service, @Component)
 * - NO port dependencies (no external calls)
 * - All data is passed as method parameters
 * - Returns domain models only
 *
 * The Application Service (GenerateEpisodeLessonService) calls the ports
 * and passes the results to this domain service for composition.
 *
 * PEDAGOGICAL ORDER:
 * 1. Vocabulary (foundation - understand words first)
 * 2. Grammar (structure - understand patterns)
 * 3. Expressions (context - understand idioms and usage)
 * 4. Exercises (practice - reinforce learning)
 * 5. Audio (pronunciation - hear correct pronunciation)
 */
public class EpisodeLessonGenerator {

    /**
     * Generate a complete lesson from extracted content.
     *
     * This method applies business rules to compose a complete lesson:
     * - Validates minimum content requirements
     * - Orders content pedagogically
     * - Ensures consistency across all components
     *
     * @param vocabulary Extracted vocabulary items (should be 15-25 items)
     * @param grammar Extracted grammar points (should be 4-6 points)
     * @param expressions Extracted expressions (should be 6-10 items)
     * @param exercises Generated exercises (should be 12-15 exercises)
     * @return A complete lesson ready for persistence
     * @throws IllegalArgumentException if content doesn't meet minimum requirements
     */
    public GeneratedLesson generate(
            List<ExtractedVocabulary> vocabulary,
            List<ExtractedGrammar> grammar,
            List<ExtractedExpression> expressions,
            List<GeneratedExercise> exercises) {

        // Business rule: Validate minimum content
        validateContent(vocabulary, grammar, expressions, exercises);

        // Business rule: Ensure immutability by creating defensive copies
        List<ExtractedVocabulary> immutableVocab = List.copyOf(vocabulary);
        List<ExtractedGrammar> immutableGrammar = List.copyOf(grammar);
        List<ExtractedExpression> immutableExpressions = List.copyOf(expressions);
        List<GeneratedExercise> immutableExercises = List.copyOf(exercises);

        return new GeneratedLesson(
                immutableVocab,
                immutableGrammar,
                immutableExpressions,
                immutableExercises
        );
    }

    /**
     * Validate that extracted content meets pedagogical minimum requirements.
     *
     * Business rules:
     * - At least 10 vocabulary items (learning requires sufficient examples)
     * - At least 3 grammar points (need variety for understanding)
     * - At least 5 expressions (enough for context understanding)
     * - At least 10 exercises (sufficient practice opportunities)
     */
    private void validateContent(
            List<ExtractedVocabulary> vocabulary,
            List<ExtractedGrammar> grammar,
            List<ExtractedExpression> expressions,
            List<GeneratedExercise> exercises) {

        List<String> errors = new ArrayList<>();

        if (vocabulary == null || vocabulary.size() < 10) {
            errors.add("Insufficient vocabulary items: " +
                    (vocabulary == null ? 0 : vocabulary.size()) +
                    " (minimum 10 required)");
        }

        if (grammar == null || grammar.size() < 3) {
            errors.add("Insufficient grammar points: " +
                    (grammar == null ? 0 : grammar.size()) +
                    " (minimum 3 required)");
        }

        if (expressions == null || expressions.size() < 5) {
            errors.add("Insufficient expressions: " +
                    (expressions == null ? 0 : expressions.size()) +
                    " (minimum 5 required)");
        }

        if (exercises == null || exercises.size() < 10) {
            errors.add("Insufficient exercises: " +
                    (exercises == null ? 0 : exercises.size()) +
                    " (minimum 10 required)");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Lesson content does not meet minimum requirements: " +
                            String.join(", ", errors)
            );
        }
    }

    /**
     * Calculate the expected total points for a lesson.
     * This can be used for progress tracking and gamification.
     */
    public int calculateTotalPoints(GeneratedLesson lesson) {
        return lesson.exercises().stream()
                .mapToInt(GeneratedExercise::points)
                .sum();
    }

    /**
     * Check if a lesson has sufficient content for a complete learning experience.
     * More lenient than validateContent - used for warnings rather than errors.
     */
    public boolean isHighQuality(GeneratedLesson lesson) {
        return lesson.vocabulary().size() >= 15 &&
               lesson.grammarPoints().size() >= 4 &&
               lesson.expressions().size() >= 6 &&
               lesson.exercises().size() >= 12;
    }
}
