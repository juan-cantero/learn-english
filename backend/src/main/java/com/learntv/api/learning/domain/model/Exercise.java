package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Exercise domain entity with answer checking logic.
 * Business rules for validation live here, not in services.
 */
public class Exercise {

    private final UUID id;
    private final UUID episodeId;
    private final ExerciseType type;
    private final String question;
    private final String correctAnswer;
    private final String options; // JSON array for multiple choice
    private final String matchingPairs; // JSON for matching exercises
    private final int points;

    private Exercise(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.type = Objects.requireNonNull(builder.type, "type is required");
        this.question = Objects.requireNonNull(builder.question, "question is required");
        this.correctAnswer = builder.correctAnswer;
        this.options = builder.options;
        this.matchingPairs = builder.matchingPairs;
        this.points = builder.points > 0 ? builder.points : 10;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public ExerciseType getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getOptions() {
        return options;
    }

    public String getMatchingPairs() {
        return matchingPairs;
    }

    public int getPoints() {
        return points;
    }

    // ==================== Business Logic ====================

    /**
     * Check if the user's answer is correct.
     * Handles different exercise types with appropriate comparison logic.
     */
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return false;
        }

        return switch (type) {
            case FILL_IN_BLANK -> checkFillInBlank(userAnswer);
            case MULTIPLE_CHOICE -> checkMultipleChoice(userAnswer);
            case MATCHING -> checkMatching(userAnswer);
            case LISTENING -> checkListening(userAnswer);
        };
    }

    /**
     * Fill-in-blank: case-insensitive, trimmed comparison.
     * Allows for common typos and alternative spellings.
     */
    private boolean checkFillInBlank(String userAnswer) {
        if (correctAnswer == null) return false;

        String normalized = normalizeAnswer(userAnswer);
        String expected = normalizeAnswer(correctAnswer);

        // Exact match after normalization
        if (normalized.equals(expected)) return true;

        // Allow for minor typos (1 character difference for words > 4 chars)
        if (expected.length() > 4) {
            return levenshteinDistance(normalized, expected) <= 1;
        }

        return false;
    }

    /**
     * Multiple choice: exact match against correct answer.
     */
    private boolean checkMultipleChoice(String userAnswer) {
        if (correctAnswer == null) return false;
        return normalizeAnswer(userAnswer).equals(normalizeAnswer(correctAnswer));
    }

    /**
     * Matching: expects JSON with user's matches, validates against matchingPairs.
     * Format: [{"term":"...", "definition":"..."}]
     */
    private boolean checkMatching(String userAnswer) {
        // For matching, we compare the JSON structure
        // Simplified: just check if all pairs match
        if (matchingPairs == null || userAnswer == null) return false;
        return normalizeAnswer(userAnswer).equals(normalizeAnswer(matchingPairs));
    }

    /**
     * Listening: case-insensitive comparison with some flexibility.
     */
    private boolean checkListening(String userAnswer) {
        if (correctAnswer == null) return false;
        return normalizeAnswer(userAnswer).equals(normalizeAnswer(correctAnswer));
    }

    private String normalizeAnswer(String answer) {
        return answer.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove punctuation
                .replaceAll("\\s+", " ");       // Normalize whitespace
    }

    /**
     * Simple Levenshtein distance for typo tolerance.
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    /**
     * Check if this is an interactive exercise type.
     */
    public boolean isInteractive() {
        return type == ExerciseType.FILL_IN_BLANK ||
               type == ExerciseType.MATCHING ||
               type == ExerciseType.LISTENING;
    }

    /**
     * Get a hint for the exercise (first letter for fill-in-blank).
     */
    public String getHint() {
        if (type == ExerciseType.FILL_IN_BLANK && correctAnswer != null && !correctAnswer.isEmpty()) {
            return correctAnswer.substring(0, 1).toUpperCase() + "...";
        }
        return null;
    }

    public static class Builder {
        private UUID id;
        private UUID episodeId;
        private ExerciseType type;
        private String question;
        private String correctAnswer;
        private String options;
        private String matchingPairs;
        private int points = 10;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder type(ExerciseType type) {
            this.type = type;
            return this;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder correctAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
            return this;
        }

        public Builder options(String options) {
            this.options = options;
            return this;
        }

        public Builder matchingPairs(String matchingPairs) {
            this.matchingPairs = matchingPairs;
            return this;
        }

        public Builder points(int points) {
            this.points = points;
            return this;
        }

        public Exercise build() {
            return new Exercise(this);
        }
    }
}
