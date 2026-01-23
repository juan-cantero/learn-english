package com.learntv.api.generation.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Search results for TV shows")
public record ShowSearchResponse(
        @Schema(description = "List of matching shows")
        List<ShowDto> shows,

        @Schema(description = "Total number of results found", example = "5")
        int totalResults
) {}
