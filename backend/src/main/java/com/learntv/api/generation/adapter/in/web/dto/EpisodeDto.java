package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.application.port.out.ShowMetadataPort.Episode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Episode information")
public record EpisodeDto(
        @Schema(description = "Episode number", example = "1")
        int episodeNumber,

        @Schema(description = "Episode title", example = "Pilot")
        String title,

        @Schema(description = "Episode overview/description")
        String overview,

        @Schema(description = "Episode runtime in minutes", example = "45", nullable = true)
        Integer runtime
) {
    public static EpisodeDto fromDomain(Episode episode) {
        return new EpisodeDto(
                episode.episodeNumber(),
                episode.title(),
                episode.overview(),
                episode.runtime()
        );
    }
}
