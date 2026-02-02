package com.learntv.api.progress.adapter.in.web;

import com.learntv.api.learning.domain.model.Episode;
import com.learntv.api.progress.domain.model.UserProgress;

import java.time.Instant;

public record UserProgressResponse(
        String id,
        String episodeId,
        EpisodeMetadata episode,
        int vocabularyScore,
        int grammarScore,
        int expressionsScore,
        int exercisesScore,
        int totalPoints,
        boolean completed,
        Instant lastAccessed
) {

    public record EpisodeMetadata(
            String title,
            String showSlug,
            String episodeSlug,
            int seasonNumber,
            int episodeNumber
    ) {
        public static EpisodeMetadata fromEpisode(Episode episode) {
            return new EpisodeMetadata(
                    episode.getTitle(),
                    episode.getShowSlug(),
                    episode.getSlug(),
                    episode.getSeasonNumber(),
                    episode.getEpisodeNumber()
            );
        }
    }

    public static UserProgressResponse fromDomain(UserProgress progress, Episode episode) {
        return new UserProgressResponse(
                progress.getId().toString(),
                progress.getEpisodeId().toString(),
                episode != null ? EpisodeMetadata.fromEpisode(episode) : null,
                progress.getVocabularyScore(),
                progress.getGrammarScore(),
                progress.getExpressionsScore(),
                progress.getExercisesScore(),
                progress.getTotalPoints(),
                progress.isCompleted(),
                progress.getLastAccessed()
        );
    }

    /**
     * Legacy method for backward compatibility (without episode metadata).
     */
    public static UserProgressResponse fromDomain(UserProgress progress) {
        return fromDomain(progress, null);
    }
}
