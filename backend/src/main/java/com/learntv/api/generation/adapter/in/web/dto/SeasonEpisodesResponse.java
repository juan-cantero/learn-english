package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.application.port.out.ShowMetadataPort.SeasonWithEpisodes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Season details with episodes")
public record SeasonEpisodesResponse(
        @Schema(description = "TMDB show ID", example = "250307")
        String tmdbId,

        @Schema(description = "Show title", example = "The Pitt")
        String showTitle,

        @Schema(description = "Season number", example = "1")
        int season,

        @Schema(description = "List of episodes")
        List<EpisodeDto> episodes
) {
    public static SeasonEpisodesResponse fromDomain(SeasonWithEpisodes seasonWithEpisodes) {
        List<EpisodeDto> episodeDtos = seasonWithEpisodes.episodes().stream()
                .map(EpisodeDto::fromDomain)
                .toList();

        return new SeasonEpisodesResponse(
                seasonWithEpisodes.tmdbId(),
                seasonWithEpisodes.showTitle(),
                seasonWithEpisodes.seasonNumber(),
                episodeDtos
        );
    }
}
