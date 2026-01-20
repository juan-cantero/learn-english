package com.learntv.api.progress.application.usecase;

import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.UUID;

/**
 * Use case: Update user's learning progress for an episode.
 *
 * Handles:
 * - Creating new progress records
 * - Updating existing progress
 * - Marking episodes as complete
 */
public class UpdateProgressUseCase {

    private final UserProgressRepository progressRepository;

    public UpdateProgressUseCase(UserProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public UserProgress execute(String userId, UUID episodeId, ProgressUpdate update) {
        // Get or create progress
        UserProgress progress = progressRepository.findByUserIdAndEpisodeId(userId, episodeId)
                .orElseGet(() -> UserProgress.builder()
                        .userId(userId)
                        .episodeId(episodeId)
                        .build());

        // Apply updates using domain logic
        if (update.category() != null && update.points() > 0) {
            progress.updateProgress(update.category(), update.points());
        }

        if (update.markComplete()) {
            progress.markCompleted();
        }

        return progressRepository.save(progress);
    }

    /**
     * Input for progress update.
     */
    public record ProgressUpdate(
            String category,
            int points,
            boolean markComplete
    ) {
        public static ProgressUpdate forCategory(String category, int points) {
            return new ProgressUpdate(category, points, false);
        }

        public static ProgressUpdate complete() {
            return new ProgressUpdate(null, 0, true);
        }
    }
}
