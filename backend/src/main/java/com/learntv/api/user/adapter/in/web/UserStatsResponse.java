package com.learntv.api.user.adapter.in.web;

import com.learntv.api.user.domain.model.UserStats;

import java.time.LocalDate;

public record UserStatsResponse(
        int totalEpisodesCompleted,
        int totalExercisesCompleted,
        int totalWordsLearned,
        int currentStreakDays,
        int longestStreakDays,
        LocalDate lastActivityDate
) {
    public static UserStatsResponse fromDomain(UserStats stats) {
        return new UserStatsResponse(
                stats.getTotalEpisodesCompleted(),
                stats.getTotalExercisesCompleted(),
                stats.getTotalWordsLearned(),
                stats.getCurrentStreakDays(),
                stats.getLongestStreakDays(),
                stats.getLastActivityDate()
        );
    }
}
