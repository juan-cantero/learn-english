package com.learntv.api.catalog.adapter.in.web;

import com.learntv.api.catalog.application.usecase.ViewShowDetailsUseCase;
import com.learntv.api.learning.domain.model.Episode;

import java.util.List;

public record ShowWithEpisodesResponse(
        ShowResponse show,
        List<EpisodeSummary> episodes,
        int seasonCount,
        int episodeCount
) {

    public static ShowWithEpisodesResponse fromDomain(ViewShowDetailsUseCase.ShowWithEpisodes result) {
        List<EpisodeSummary> episodes = result.episodes().stream()
                .map(EpisodeSummary::fromDomain)
                .toList();

        return new ShowWithEpisodesResponse(
                ShowResponse.fromDomain(result.show()),
                episodes,
                result.getSeasonCount(),
                result.getEpisodeCount()
        );
    }

    public record EpisodeSummary(
            String id,
            int seasonNumber,
            int episodeNumber,
            String title,
            String slug,
            int durationMinutes
    ) {
        public static EpisodeSummary fromDomain(Episode episode) {
            return new EpisodeSummary(
                    episode.getId().toString(),
                    episode.getSeasonNumber(),
                    episode.getEpisodeNumber(),
                    episode.getTitle(),
                    episode.getSlug(),
                    episode.getDurationMinutes()
            );
        }
    }
}
