package com.learntv.api.progress.application.usecase;

import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.ProgressSnapshot;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: Get user's learning progress.
 *
 * Returns overall progress snapshot or specific episode progress.
 */
public class GetUserProgressUseCase {

    private final UserProgressRepository progressRepository;

    public GetUserProgressUseCase(UserProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    /**
     * Get overall progress for a user across all episodes.
     */
    public ProgressSnapshot execute(UUID userId) {
        List<UserProgress> progressList = progressRepository.findByUserId(userId);
        return ProgressSnapshot.fromProgress(userId, progressList);
    }

    /**
     * Get progress for a specific episode.
     */
    public Optional<UserProgress> execute(UUID userId, UUID episodeId) {
        return progressRepository.findByUserIdAndEpisodeId(userId, episodeId);
    }
}
