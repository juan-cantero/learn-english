package com.learntv.api.progress.adapter.in.web;

import com.learntv.api.progress.domain.model.ProgressSnapshot;

import java.util.List;

public record ProgressSnapshotResponse(
        String userId,
        int totalPoints,
        int completedEpisodes,
        List<UserProgressResponse> episodes
) {

    public static ProgressSnapshotResponse fromDomain(ProgressSnapshot snapshot) {
        List<UserProgressResponse> episodes = snapshot.episodeProgress().stream()
                .map(UserProgressResponse::fromDomain)
                .toList();

        return new ProgressSnapshotResponse(
                snapshot.userId(),
                snapshot.totalPoints(),
                snapshot.completedEpisodes(),
                episodes
        );
    }
}
