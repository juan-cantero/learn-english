package com.learntv.api.learning.application.usecase;

import com.learntv.api.learning.application.port.LessonQueryPort;
import com.learntv.api.learning.domain.exception.EpisodeNotFoundException;
import com.learntv.api.learning.domain.exception.ExerciseNotFoundException;
import com.learntv.api.learning.domain.model.Exercise;
import com.learntv.api.learning.domain.model.Lesson;
import com.learntv.api.progress.application.port.UserProgressRepository;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.UUID;

/**
 * Use case: Check an exercise answer and update user progress.
 *
 * Orchestrates:
 * - Loading the lesson to find the exercise
 * - Checking the answer using domain logic
 * - Updating user progress if correct
 * - Determining if lesson is now complete
 */
public class CheckExerciseAnswerUseCase {

    private final LessonQueryPort lessonQueryPort;
    private final UserProgressRepository progressRepository;

    public CheckExerciseAnswerUseCase(LessonQueryPort lessonQueryPort,
                                       UserProgressRepository progressRepository) {
        this.lessonQueryPort = lessonQueryPort;
        this.progressRepository = progressRepository;
    }

    public AnswerResult execute(UUID userId, String showSlug, String episodeSlug,
                                 UUID exerciseId, String userAnswer) {
        // Load lesson to get the exercise
        Lesson lesson = lessonQueryPort.loadFullLesson(showSlug, episodeSlug)
                .orElseThrow(() -> new EpisodeNotFoundException(showSlug, episodeSlug));

        // Find the exercise within the lesson (domain logic)
        Exercise exercise = lesson.findExercise(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        // Check answer using domain logic
        boolean correct = exercise.checkAnswer(userAnswer);
        int pointsEarned = correct ? exercise.getPoints() : 0;

        // Update progress if correct
        UserProgress progress = getOrCreateProgress(userId, lesson.getEpisode().getId().value());

        if (correct) {
            progress.updateProgress("exercises", pointsEarned);
            progress = progressRepository.save(progress);
        }

        // Check if lesson is now complete
        boolean lessonComplete = lesson.isComplete(progress);
        if (lessonComplete && !progress.isCompleted()) {
            progress.markCompleted();
            progress = progressRepository.save(progress);
        }

        return new AnswerResult(
                exerciseId,
                correct,
                pointsEarned,
                correct ? null : exercise.getCorrectAnswer(),
                progress.getTotalPoints(),
                lesson.getTotalPoints(),
                lessonComplete
        );
    }

    private UserProgress getOrCreateProgress(UUID userId, UUID episodeId) {
        return progressRepository.findByUserIdAndEpisodeId(userId, episodeId)
                .orElseGet(() -> UserProgress.builder()
                        .userId(userId)
                        .episodeId(episodeId)
                        .build());
    }

    /**
     * Result of checking an exercise answer.
     */
    public record AnswerResult(
            UUID exerciseId,
            boolean correct,
            int pointsEarned,
            String correctAnswer,
            int totalProgressPoints,
            int lessonTotalPoints,
            boolean lessonComplete
    ) {
        public double progressPercentage() {
            if (lessonTotalPoints == 0) return 0;
            return (totalProgressPoints * 100.0) / lessonTotalPoints;
        }
    }
}
