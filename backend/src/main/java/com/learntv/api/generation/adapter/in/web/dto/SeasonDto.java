package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.application.port.out.ShowMetadataPort.Season;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Season information")
public record SeasonDto(
        @Schema(description = "Season number", example = "1")
        int seasonNumber,

        @Schema(description = "Season name", example = "Season 1")
        String name,

        @Schema(description = "Number of episodes in this season", example = "15")
        int episodeCount
) {
    public static SeasonDto fromDomain(Season season) {
        return new SeasonDto(
                season.seasonNumber(),
                season.name(),
                season.episodeCount()
        );
    }
}
