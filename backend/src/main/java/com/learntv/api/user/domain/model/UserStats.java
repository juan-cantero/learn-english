package com.learntv.api.user.domain.model;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserStats {

    private final UUID userId;
    private int totalEpisodesCompleted;
    private int totalExercisesCompleted;
    private int totalWordsLearned;
    private int currentStreakDays;
    private int longestStreakDays;
    private LocalDate lastActivityDate;
    private Instant updatedAt;

    private UserStats(Builder builder) {
        this.userId = Objects.requireNonNull(builder.userId, "userId is required");
        this.totalEpisodesCompleted = builder.totalEpisodesCompleted;
        this.totalExercisesCompleted = builder.totalExercisesCompleted;
        this.totalWordsLearned = builder.totalWordsLearned;
        this.currentStreakDays = builder.currentStreakDays;
        this.longestStreakDays = builder.longestStreakDays;
        this.lastActivityDate = builder.lastActivityDate;
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UserStats createEmpty(UUID userId) {
        return builder()
                .userId(userId)
                .build();
    }

    public void recordActivity() {
        LocalDate today = LocalDate.now();

        if (lastActivityDate == null) {
            currentStreakDays = 1;
        } else if (lastActivityDate.equals(today.minusDays(1))) {
            currentStreakDays++;
        } else if (!lastActivityDate.equals(today)) {
            currentStreakDays = 1;
        }

        if (currentStreakDays > longestStreakDays) {
            longestStreakDays = currentStreakDays;
        }

        lastActivityDate = today;
        updatedAt = Instant.now();
    }

    public void incrementEpisodesCompleted() {
        totalEpisodesCompleted++;
        recordActivity();
    }

    public void incrementExercisesCompleted() {
        totalExercisesCompleted++;
        recordActivity();
    }

    public void incrementWordsLearned(int count) {
        totalWordsLearned += count;
        recordActivity();
    }

    // Getters
    public UUID getUserId() {
        return userId;
    }

    public int getTotalEpisodesCompleted() {
        return totalEpisodesCompleted;
    }

    public int getTotalExercisesCompleted() {
        return totalExercisesCompleted;
    }

    public int getTotalWordsLearned() {
        return totalWordsLearned;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public int getLongestStreakDays() {
        return longestStreakDays;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private UUID userId;
        private int totalEpisodesCompleted;
        private int totalExercisesCompleted;
        private int totalWordsLearned;
        private int currentStreakDays;
        private int longestStreakDays;
        private LocalDate lastActivityDate;
        private Instant updatedAt;

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder totalEpisodesCompleted(int totalEpisodesCompleted) {
            this.totalEpisodesCompleted = totalEpisodesCompleted;
            return this;
        }

        public Builder totalExercisesCompleted(int totalExercisesCompleted) {
            this.totalExercisesCompleted = totalExercisesCompleted;
            return this;
        }

        public Builder totalWordsLearned(int totalWordsLearned) {
            this.totalWordsLearned = totalWordsLearned;
            return this;
        }

        public Builder currentStreakDays(int currentStreakDays) {
            this.currentStreakDays = currentStreakDays;
            return this;
        }

        public Builder longestStreakDays(int longestStreakDays) {
            this.longestStreakDays = longestStreakDays;
            return this;
        }

        public Builder lastActivityDate(LocalDate lastActivityDate) {
            this.lastActivityDate = lastActivityDate;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserStats build() {
            return new UserStats(this);
        }
    }
}
