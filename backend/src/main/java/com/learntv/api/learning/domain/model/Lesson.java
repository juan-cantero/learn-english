package com.learntv.api.learning.domain.model;

import com.learntv.api.progress.domain.model.UserProgress;

import java.util.*;

/**
 * Lesson is an aggregate that combines all learning content for an episode.
 * Contains business logic for scoring, completion, and progress evaluation.
 */
public class Lesson {

    private static final double COMPLETION_THRESHOLD = 0.8; // 80% to complete

    private final Episode episode;
    private final List<Vocabulary> vocabulary;
    private final List<GrammarPoint> grammarPoints;
    private final List<Expression> expressions;
    private final List<Exercise> exercises;

    public Lesson(Episode episode,
                  List<Vocabulary> vocabulary,
                  List<GrammarPoint> grammarPoints,
                  List<Expression> expressions,
                  List<Exercise> exercises) {
        this.episode = Objects.requireNonNull(episode, "episode is required");
        this.vocabulary = vocabulary != null ? List.copyOf(vocabulary) : List.of();
        this.grammarPoints = grammarPoints != null ? List.copyOf(grammarPoints) : List.of();
        this.expressions = expressions != null ? List.copyOf(expressions) : List.of();
        this.exercises = exercises != null ? List.copyOf(exercises) : List.of();
    }

    // ==================== Getters ====================

    public Episode getEpisode() {
        return episode;
    }

    public List<Vocabulary> getVocabulary() {
        return vocabulary;
    }

    public List<GrammarPoint> getGrammarPoints() {
        return grammarPoints;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    // ==================== Business Logic ====================

    /**
     * Total possible points for all exercises in this lesson.
     */
    public int getTotalPoints() {
        return exercises.stream()
                .mapToInt(Exercise::getPoints)
                .sum();
    }

    /**
     * Calculate score based on user answers.
     * @param userAnswers map of exerciseId -> userAnswer
     * @return total points earned
     */
    public int calculateScore(Map<UUID, String> userAnswers) {
        return exercises.stream()
                .mapToInt(exercise -> {
                    String userAnswer = userAnswers.get(exercise.getId());
                    return exercise.checkAnswer(userAnswer) ? exercise.getPoints() : 0;
                })
                .sum();
    }

    /**
     * Check each exercise and return detailed results.
     */
    public List<ExerciseResult> evaluateAnswers(Map<UUID, String> userAnswers) {
        return exercises.stream()
                .map(exercise -> {
                    String userAnswer = userAnswers.get(exercise.getId());
                    boolean correct = exercise.checkAnswer(userAnswer);
                    return new ExerciseResult(
                            exercise.getId(),
                            correct,
                            correct ? exercise.getPoints() : 0,
                            correct ? null : exercise.getCorrectAnswer()
                    );
                })
                .toList();
    }

    /**
     * Check if user has completed this lesson based on progress.
     * Completion requires 80% of total points.
     */
    public boolean isComplete(UserProgress progress) {
        if (progress == null) return false;
        int requiredPoints = (int) (getTotalPoints() * COMPLETION_THRESHOLD);
        return progress.getTotalPoints() >= requiredPoints;
    }

    /**
     * Calculate completion percentage based on progress.
     */
    public double getCompletionPercentage(UserProgress progress) {
        if (progress == null || getTotalPoints() == 0) return 0.0;
        return Math.min(100.0, (progress.getTotalPoints() * 100.0) / getTotalPoints());
    }

    /**
     * Get exercises by type.
     */
    public List<Exercise> getExercisesByType(ExerciseType type) {
        return exercises.stream()
                .filter(e -> e.getType() == type)
                .toList();
    }

    /**
     * Get vocabulary by category.
     */
    public List<Vocabulary> getVocabularyByCategory(VocabularyCategory category) {
        return vocabulary.stream()
                .filter(v -> v.getCategory() == category)
                .toList();
    }

    /**
     * Find exercise by ID.
     */
    public Optional<Exercise> findExercise(UUID exerciseId) {
        return exercises.stream()
                .filter(e -> e.getId().equals(exerciseId))
                .findFirst();
    }

    // ==================== Content Statistics ====================

    public int getVocabularyCount() {
        return vocabulary.size();
    }

    public int getGrammarPointCount() {
        return grammarPoints.size();
    }

    public int getExpressionCount() {
        return expressions.size();
    }

    public int getExerciseCount() {
        return exercises.size();
    }

    // ==================== Value Objects ====================

    public record ExerciseResult(
            UUID exerciseId,
            boolean correct,
            int pointsEarned,
            String correctAnswer
    ) {}
}
