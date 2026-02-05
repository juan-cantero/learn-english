package com.learntv.api.progress.adapter.in.web;

import com.learntv.api.learning.domain.model.Episode;
import com.learntv.api.progress.domain.model.ProgressSnapshot;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProgressSnapshotResponse(
        String userId,
        int totalPoints,
        int totalLessonsStarted,
        int totalLessonsCompleted,
        List<UserProgressResponse> recentProgress
) {

    public static ProgressSnapshotResponse fromDomain(ProgressSnapshot snapshot, Map<UUID, Episode> episodesById) {
        List<UserProgressResponse> recentProgress = snapshot.episodeProgress().stream()
                .sorted((a, b) -> b.getLastAccessed().compareTo(a.getLastAccessed()))
                .limit(10)
                .map(progress -> {
                    Episode episode = episodesById.get(progress.getEpisodeId());
                    return UserProgressResponse.fromDomain(progress, episode);
                })
                .toList();

        int completedCount = (int) snapshot.episodeProgress().stream()
                .filter(UserProgress::isCompleted)
                .count();

        return new ProgressSnapshotResponse(
                snapshot.userId().toString(),
                snapshot.totalPoints(),
                snapshot.episodeProgress().size(),
                completedCount,
                recentProgress
        );
    }

    /**
     * Legacy method for backward compatibility (without episode metadata).
     */
    public static ProgressSnapshotResponse fromDomain(ProgressSnapshot snapshot) {
        return fromDomain(snapshot, Map.of());
    }
}
