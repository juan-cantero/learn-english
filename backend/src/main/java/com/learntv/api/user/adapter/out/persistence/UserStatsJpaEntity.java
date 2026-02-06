package com.learntv.api.user.adapter.out.persistence;

import com.learntv.api.user.domain.model.UserStats;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_stats")
public class UserStatsJpaEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "total_episodes_completed")
    private int totalEpisodesCompleted;

    @Column(name = "total_exercises_completed")
    private int totalExercisesCompleted;

    @Column(name = "total_words_learned")
    private int totalWordsLearned;

    @Column(name = "current_streak_days")
    private int currentStreakDays;

    @Column(name = "longest_streak_days")
    private int longestStreakDays;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected UserStatsJpaEntity() {
    }

    public static UserStatsJpaEntity fromDomain(UserStats stats) {
        UserStatsJpaEntity entity = new UserStatsJpaEntity();
        entity.userId = stats.getUserId();
        entity.totalEpisodesCompleted = stats.getTotalEpisodesCompleted();
        entity.totalExercisesCompleted = stats.getTotalExercisesCompleted();
        entity.totalWordsLearned = stats.getTotalWordsLearned();
        entity.currentStreakDays = stats.getCurrentStreakDays();
        entity.longestStreakDays = stats.getLongestStreakDays();
        entity.lastActivityDate = stats.getLastActivityDate();
        entity.updatedAt = stats.getUpdatedAt();
        return entity;
    }

    public UserStats toDomain() {
        return UserStats.builder()
                .userId(userId)
                .totalEpisodesCompleted(totalEpisodesCompleted)
                .totalExercisesCompleted(totalExercisesCompleted)
                .totalWordsLearned(totalWordsLearned)
                .currentStreakDays(currentStreakDays)
                .longestStreakDays(longestStreakDays)
                .lastActivityDate(lastActivityDate)
                .updatedAt(updatedAt)
                .build();
    }

    public UUID getUserId() {
        return userId;
    }
}
