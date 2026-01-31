package com.learntv.api.generation.adapter.in.web.dto;

import com.learntv.api.generation.application.port.out.ShowMetadataPort.ShowWithSeasons;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Show details with seasons")
public record ShowSeasonsResponse(
        @Schema(description = "Basic show information")
        ShowDto show,

        @Schema(description = "List of seasons")
        List<SeasonDto> seasons
) {
    public static ShowSeasonsResponse fromDomain(ShowWithSeasons showWithSeasons) {
        ShowDto showDto = new ShowDto(
                showWithSeasons.tmdbId(),
                null,  // imdbId not available in this context
                showWithSeasons.title(),
                showWithSeasons.overview(),
                showWithSeasons.posterUrl(),
                showWithSeasons.year(),
                showWithSeasons.seasons().size()
        );

        List<SeasonDto> seasonDtos = showWithSeasons.seasons().stream()
                .map(SeasonDto::fromDomain)
                .toList();

        return new ShowSeasonsResponse(showDto, seasonDtos);
    }
}
