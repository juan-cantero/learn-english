package com.learntv.api.progress.domain.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregate of all user progress across episodes.
 */
public record ProgressSnapshot(
        String userId,
        int totalPoints,
        int completedEpisodes,
        List<UserProgress> episodeProgress
) {

    public static ProgressSnapshot fromProgress(String userId, List<UserProgress> progressList) {
        int totalPoints = progressList.stream()
                .mapToInt(UserProgress::getTotalPoints)
                .sum();

        int completedCount = (int) progressList.stream()
                .filter(UserProgress::isCompleted)
                .count();

        return new ProgressSnapshot(userId, totalPoints, completedCount, progressList);
    }

    public Map<String, Integer> getProgressByEpisode() {
        return episodeProgress.stream()
                .collect(Collectors.toMap(
                        p -> p.getEpisodeId().toString(),
                        UserProgress::getTotalPoints
                ));
    }
}
