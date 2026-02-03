package com.learntv.api.learning.adapter.in.web;

import com.learntv.api.learning.application.usecase.ViewEpisodeLessonUseCase;
import com.learntv.api.learning.domain.model.*;
import com.learntv.api.progress.domain.model.UserProgress;

import java.util.List;

public record LessonWithProgressResponse(
        EpisodeResponse episode,
        List<VocabularyResponse> vocabulary,
        List<GrammarPointResponse> grammarPoints,
        List<ExpressionResponse> expressions,
        List<ExerciseResponse> exercises,
        ProgressSummary progress
) {

    public static LessonWithProgressResponse fromDomain(ViewEpisodeLessonUseCase.LessonWithProgress result) {
        Lesson lesson = result.lesson();

        return new LessonWithProgressResponse(
                EpisodeResponse.fromDomain(lesson.getEpisode()),
                lesson.getVocabulary().stream().map(VocabularyResponse::fromDomain).toList(),
                lesson.getGrammarPoints().stream().map(GrammarPointResponse::fromDomain).toList(),
                lesson.getExpressions().stream().map(ExpressionResponse::fromDomain).toList(),
                lesson.getExercises().stream().map(ExerciseResponse::fromDomain).toList(),
                ProgressSummary.fromResult(result)
        );
    }

    public record EpisodeResponse(
            String id,
            String showSlug,
            int seasonNumber,
            int episodeNumber,
            String title,
            String slug,
            String synopsis,
            int durationMinutes
    ) {
        public static EpisodeResponse fromDomain(Episode episode) {
            return new EpisodeResponse(
                    episode.getId().toString(),
                    episode.getShowSlug(),
                    episode.getSeasonNumber(),
                    episode.getEpisodeNumber(),
                    episode.getTitle(),
                    episode.getSlug(),
                    episode.getSynopsis(),
                    episode.getDurationMinutes()
            );
        }
    }

    public record VocabularyResponse(
            String id,
            String term,
            String definition,
            String phonetic,
            String category,
            String exampleSentence,
            String contextTimestamp,
            String audioUrl
    ) {
        public static VocabularyResponse fromDomain(Vocabulary vocab) {
            return new VocabularyResponse(
                    vocab.getId().toString(),
                    vocab.getTerm(),
                    vocab.getDefinition(),
                    vocab.getPhonetic(),
                    vocab.getCategory().name(),
                    vocab.getExampleSentence(),
                    vocab.getContextTimestamp(),
                    vocab.getAudioUrl()
            );
        }
    }

    public record GrammarPointResponse(
            String id,
            String title,
            String explanation,
            String structure,
            String example,
            String contextQuote
    ) {
        public static GrammarPointResponse fromDomain(GrammarPoint gp) {
            return new GrammarPointResponse(
                    gp.getId().toString(),
                    gp.getTitle(),
                    gp.getExplanation(),
                    gp.getStructure(),
                    gp.getExample(),
                    gp.getContextQuote()
            );
        }
    }

    public record ExpressionResponse(
            String id,
            String phrase,
            String meaning,
            String contextQuote,
            String usageNote,
            String audioUrl
    ) {
        public static ExpressionResponse fromDomain(Expression expr) {
            return new ExpressionResponse(
                    expr.getId().toString(),
                    expr.getPhrase(),
                    expr.getMeaning(),
                    expr.getContextQuote(),
                    expr.getUsageNote(),
                    expr.getAudioUrl()
            );
        }
    }

    public record ExerciseResponse(
            String id,
            String type,
            String question,
            String options,
            String matchingPairs,
            int points,
            String hint,
            String audioUrl
    ) {
        public static ExerciseResponse fromDomain(Exercise ex) {
            return new ExerciseResponse(
                    ex.getId().toString(),
                    ex.getType().name(),
                    ex.getQuestion(),
                    ex.getOptions(),
                    ex.getMatchingPairs(),
                    ex.getPoints(),
                    ex.getHint(),
                    ex.getAudioUrl()
            );
        }
    }

    public record ProgressSummary(
            int earnedPoints,
            int totalPoints,
            double completionPercentage,
            boolean isComplete,
            int vocabularyScore,
            int grammarScore,
            int expressionsScore,
            int exercisesScore
    ) {
        public static ProgressSummary fromResult(ViewEpisodeLessonUseCase.LessonWithProgress result) {
            UserProgress progress = result.progress();

            return new ProgressSummary(
                    result.getEarnedPoints(),
                    result.getTotalPoints(),
                    result.completionPercentage(),
                    result.isComplete(),
                    progress != null ? progress.getVocabularyScore() : 0,
                    progress != null ? progress.getGrammarScore() : 0,
                    progress != null ? progress.getExpressionsScore() : 0,
                    progress != null ? progress.getExercisesScore() : 0
            );
        }
    }
}
