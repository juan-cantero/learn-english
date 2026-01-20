package com.learntv.api.progress.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserProgress {

    private final UUID id;
    private final String userId;
    private final UUID episodeId;
    private int vocabularyScore;
    private int grammarScore;
    private int expressionsScore;
    private int exercisesScore;
    private int totalPoints;
    private boolean completed;
    private Instant lastAccessed;

    private UserProgress(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.userId = Objects.requireNonNull(builder.userId, "userId is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.vocabularyScore = builder.vocabularyScore;
        this.grammarScore = builder.grammarScore;
        this.expressionsScore = builder.expressionsScore;
        this.exercisesScore = builder.exercisesScore;
        this.totalPoints = builder.totalPoints;
        this.completed = builder.completed;
        this.lastAccessed = builder.lastAccessed != null ? builder.lastAccessed : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void updateProgress(String category, int points) {
        switch (category.toLowerCase()) {
            case "vocabulary" -> this.vocabularyScore += points;
            case "grammar" -> this.grammarScore += points;
            case "expressions" -> this.expressionsScore += points;
            case "exercises" -> this.exercisesScore += points;
        }
        this.totalPoints = vocabularyScore + grammarScore + expressionsScore + exercisesScore;
        this.lastAccessed = Instant.now();
    }

    public void markCompleted() {
        this.completed = true;
        this.lastAccessed = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public int getVocabularyScore() {
        return vocabularyScore;
    }

    public int getGrammarScore() {
        return grammarScore;
    }

    public int getExpressionsScore() {
        return expressionsScore;
    }

    public int getExercisesScore() {
        return exercisesScore;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public static class Builder {
        private UUID id;
        private String userId;
        private UUID episodeId;
        private int vocabularyScore;
        private int grammarScore;
        private int expressionsScore;
        private int exercisesScore;
        private int totalPoints;
        private boolean completed;
        private Instant lastAccessed;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder vocabularyScore(int vocabularyScore) {
            this.vocabularyScore = vocabularyScore;
            return this;
        }

        public Builder grammarScore(int grammarScore) {
            this.grammarScore = grammarScore;
            return this;
        }

        public Builder expressionsScore(int expressionsScore) {
            this.expressionsScore = expressionsScore;
            return this;
        }

        public Builder exercisesScore(int exercisesScore) {
            this.exercisesScore = exercisesScore;
            return this;
        }

        public Builder totalPoints(int totalPoints) {
            this.totalPoints = totalPoints;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder lastAccessed(Instant lastAccessed) {
            this.lastAccessed = lastAccessed;
            return this;
        }

        public UserProgress build() {
            return new UserProgress(this);
        }
    }
}
