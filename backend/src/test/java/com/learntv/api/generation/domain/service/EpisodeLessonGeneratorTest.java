package com.learntv.api.generation.domain.service;

import com.learntv.api.generation.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpisodeLessonGeneratorTest {

    private EpisodeLessonGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new EpisodeLessonGenerator();
    }

    @Test
    void shouldGenerateLessonWithValidContent() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(15);
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(12);

        // When
        GeneratedLesson lesson = generator.generate(vocabulary, grammar, expressions, exercises);

        // Then
        assertNotNull(lesson);
        assertEquals(15, lesson.vocabulary().size());
        assertEquals(5, lesson.grammarPoints().size());
        assertEquals(8, lesson.expressions().size());
        assertEquals(12, lesson.exercises().size());
    }

    @Test
    void shouldThrowExceptionWhenVocabularyIsBelowMinimum() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(5); // Below minimum of 10
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(12);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(vocabulary, grammar, expressions, exercises)
        );

        assertTrue(exception.getMessage().contains("Insufficient vocabulary items"));
        assertTrue(exception.getMessage().contains("minimum 10 required"));
    }

    @Test
    void shouldThrowExceptionWhenGrammarIsBelowMinimum() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(15);
        List<ExtractedGrammar> grammar = createGrammar(2); // Below minimum of 3
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(12);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(vocabulary, grammar, expressions, exercises)
        );

        assertTrue(exception.getMessage().contains("Insufficient grammar points"));
        assertTrue(exception.getMessage().contains("minimum 3 required"));
    }

    @Test
    void shouldThrowExceptionWhenExpressionsAreBelowMinimum() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(15);
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(3); // Below minimum of 5
        List<GeneratedExercise> exercises = createExercises(12);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(vocabulary, grammar, expressions, exercises)
        );

        assertTrue(exception.getMessage().contains("Insufficient expressions"));
        assertTrue(exception.getMessage().contains("minimum 5 required"));
    }

    @Test
    void shouldThrowExceptionWhenExercisesAreBelowMinimum() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(15);
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(8); // Below minimum of 10

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(vocabulary, grammar, expressions, exercises)
        );

        assertTrue(exception.getMessage().contains("Insufficient exercises"));
        assertTrue(exception.getMessage().contains("minimum 10 required"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleContentTypesBelowMinimum() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(5); // Below minimum
        List<ExtractedGrammar> grammar = createGrammar(2); // Below minimum
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(12);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(vocabulary, grammar, expressions, exercises)
        );

        // Should contain both error messages
        assertTrue(exception.getMessage().contains("Insufficient vocabulary items"));
        assertTrue(exception.getMessage().contains("Insufficient grammar points"));
    }

    @Test
    void shouldCalculateTotalPointsCorrectly() {
        // Given
        List<ExtractedVocabulary> vocabulary = createVocabulary(15);
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = new ArrayList<>();
        exercises.add(new GeneratedExercise("FILL_IN_BLANK", "Question 1", "answer", null, 10, null));
        exercises.add(new GeneratedExercise("MULTIPLE_CHOICE", "Question 2", "answer", List.of("a", "b"), 15, null));
        exercises.add(new GeneratedExercise("MATCHING", "Question 3", "answer", null, 20, null));
        exercises.addAll(createExercises(7)); // Add 7 more to meet minimum of 10

        GeneratedLesson lesson = generator.generate(vocabulary, grammar, expressions, exercises);

        // When
        int totalPoints = generator.calculateTotalPoints(lesson);

        // Then
        assertEquals(115, totalPoints); // 10 + 15 + 20 + (7 * 10)
    }

    @Test
    void shouldIdentifyHighQualityLesson() {
        // Given - meets all high quality criteria
        List<ExtractedVocabulary> vocabulary = createVocabulary(20);
        List<ExtractedGrammar> grammar = createGrammar(5);
        List<ExtractedExpression> expressions = createExpressions(8);
        List<GeneratedExercise> exercises = createExercises(15);

        GeneratedLesson lesson = generator.generate(vocabulary, grammar, expressions, exercises);

        // When & Then
        assertTrue(generator.isHighQuality(lesson));
    }

    @Test
    void shouldIdentifyLowQualityLesson() {
        // Given - meets minimum but not high quality criteria
        List<ExtractedVocabulary> vocabulary = createVocabulary(12); // Below 15
        List<ExtractedGrammar> grammar = createGrammar(3); // Below 4
        List<ExtractedExpression> expressions = createExpressions(5); // Below 6
        List<GeneratedExercise> exercises = createExercises(11); // Below 12

        GeneratedLesson lesson = generator.generate(vocabulary, grammar, expressions, exercises);

        // When & Then
        assertFalse(generator.isHighQuality(lesson));
    }

    @Test
    void shouldCreateImmutableLesson() {
        // Given
        List<ExtractedVocabulary> vocabulary = new ArrayList<>(createVocabulary(15));
        List<ExtractedGrammar> grammar = new ArrayList<>(createGrammar(5));
        List<ExtractedExpression> expressions = new ArrayList<>(createExpressions(8));
        List<GeneratedExercise> exercises = new ArrayList<>(createExercises(12));

        GeneratedLesson lesson = generator.generate(vocabulary, grammar, expressions, exercises);

        // When - modify original lists
        vocabulary.clear();
        grammar.clear();
        expressions.clear();
        exercises.clear();

        // Then - lesson should be unaffected
        assertEquals(15, lesson.vocabulary().size());
        assertEquals(5, lesson.grammarPoints().size());
        assertEquals(8, lesson.expressions().size());
        assertEquals(12, lesson.exercises().size());
    }

    // Helper methods

    private List<ExtractedVocabulary> createVocabulary(int count) {
        List<ExtractedVocabulary> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new ExtractedVocabulary(
                    "term" + i,
                    "definition" + i,
                    "/fəˈnetɪk/",
                    "MEDICAL",
                    "Example sentence " + i,
                    null
            ));
        }
        return list;
    }

    private List<ExtractedGrammar> createGrammar(int count) {
        List<ExtractedGrammar> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new ExtractedGrammar(
                    "Grammar Point " + i,
                    "Explanation " + i,
                    "Structure " + i,
                    List.of("Example " + i)
            ));
        }
        return list;
    }

    private List<ExtractedExpression> createExpressions(int count) {
        List<ExtractedExpression> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new ExtractedExpression(
                    "phrase" + i,
                    "meaning" + i,
                    "context" + i,
                    "usage note " + i,
                    null
            ));
        }
        return list;
    }

    private List<GeneratedExercise> createExercises(int count) {
        List<GeneratedExercise> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new GeneratedExercise(
                    "FILL_IN_BLANK",
                    "Question " + i,
                    "answer" + i,
                    null,
                    10,
                    null
            ));
        }
        return list;
    }
}
