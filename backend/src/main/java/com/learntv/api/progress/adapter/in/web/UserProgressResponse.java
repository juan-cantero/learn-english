package com.learntv.api.progress.adapter.in.web;

import com.learntv.api.progress.domain.model.UserProgress;

import java.time.Instant;

public record UserProgressResponse(
        String id,
        String episodeId,
        int vocabularyScore,
        int grammarScore,
        int expressionsScore,
        int exercisesScore,
        int totalPoints,
        boolean completed,
        Instant lastAccessed
) {

    public static UserProgressResponse fromDomain(UserProgress progress) {
        return new UserProgressResponse(
                progress.getId().toString(),
                progress.getEpisodeId().toString(),
                progress.getVocabularyScore(),
                progress.getGrammarScore(),
                progress.getExpressionsScore(),
                progress.getExercisesScore(),
                progress.getTotalPoints(),
                progress.isCompleted(),
                progress.getLastAccessed()
        );
    }
}
