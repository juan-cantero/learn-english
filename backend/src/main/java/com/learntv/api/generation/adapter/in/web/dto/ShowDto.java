package com.learntv.api.generation.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TV show information")
public record ShowDto(
        @Schema(description = "TMDB ID", example = "250307")
        String tmdbId,

        @Schema(description = "IMDB ID (available when fetching details)", example = "tt31938062", nullable = true)
        String imdbId,

        @Schema(description = "Show title", example = "The Pitt")
        String title,

        @Schema(description = "Show overview/description")
        String overview,

        @Schema(description = "Poster image URL", example = "https://image.tmdb.org/t/p/w500/abc123.jpg")
        String posterUrl,

        @Schema(description = "First air year", example = "2025", nullable = true)
        Integer year,

        @Schema(description = "Number of seasons (available when fetching details)", example = "1", nullable = true)
        Integer seasons
) {}
