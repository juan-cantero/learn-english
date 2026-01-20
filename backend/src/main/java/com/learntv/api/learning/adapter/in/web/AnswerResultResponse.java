package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.learning.application.usecase.CheckExerciseAnswerUseCase;

public record AnswerResultResponse(
        String exerciseId,
        boolean correct,
        int pointsEarned,
        String correctAnswer,
        int totalProgressPoints,
        int lessonTotalPoints,
        double progressPercentage,
        boolean lessonComplete
) {

    public static AnswerResultResponse fromDomain(CheckExerciseAnswerUseCase.AnswerResult result) {
        return new AnswerResultResponse(
                result.exerciseId().toString(),
                result.correct(),
                result.pointsEarned(),
                result.correctAnswer(),
                result.totalProgressPoints(),
                result.lessonTotalPoints(),
                result.progressPercentage(),
                result.lessonComplete()
        );
    }
}
