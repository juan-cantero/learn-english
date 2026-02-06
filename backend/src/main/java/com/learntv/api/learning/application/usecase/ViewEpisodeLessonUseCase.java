package com.learntv.api.learning.application.usecase;

import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.domain.exception.EpisodeNotFoundException;
import com.learntv.api.learning.domain.model.Lesson;
import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.Optional;
import java.util.UUID;

/**
 * Use case: View an episode lesson with user's progress.
 *
 * Orchestrates:
 * - Loading the lesson content (via optimized query port)
 * - Loading user's progress for this episode
 * - Combining into a complete view with completion status
 */
public class ViewEpisodeLessonUseCase {

    private final LessonQueryPort lessonQueryPort;
    private final UserProgressRepository progressRepository;

    public ViewEpisodeLessonUseCase(LessonQueryPort lessonQueryPort,
                                     UserProgressRepository progressRepository) {
        this.lessonQueryPort = lessonQueryPort;
        this.progressRepository = progressRepository;
    }

    public LessonWithProgress execute(UUID userId, String showSlug, String episodeSlug) {
        // Load lesson via optimized query
        Lesson lesson = lessonQueryPort.loadFullLesson(showSlug, episodeSlug)
                .orElseThrow(() -> new EpisodeNotFoundException(showSlug, episodeSlug));

        // Load user's progress (may not exist yet)
        Optional<UserProgress> progressOpt = progressRepository
                .findByUserIdAndEpisodeId(userId, lesson.getEpisode().getId().value());

        UserProgress progress = progressOpt.orElse(null);

        // Use domain logic to determine completion
        boolean isComplete = lesson.isComplete(progress);
        double completionPercentage = lesson.getCompletionPercentage(progress);

        return new LessonWithProgress(
                lesson,
                progress,
                isComplete,
                completionPercentage
        );
    }

    /**
     * Result object combining lesson and progress.
     */
    public record LessonWithProgress(
            Lesson lesson,
            UserProgress progress,
            boolean isComplete,
            double completionPercentage
    ) {
        public int getEarnedPoints() {
            return progress != null ? progress.getTotalPoints() : 0;
        }

        public int getTotalPoints() {
            return lesson.getTotalPoints();
        }
    }
}
