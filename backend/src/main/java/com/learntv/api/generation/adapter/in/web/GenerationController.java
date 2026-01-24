package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.adapter.in.web.dto.ShowDto;
import com.learntv.api.generation.adapter.in.web.dto.ShowSearchResponse;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.generation.application.port.out.ShowMetadataPort.ShowSearchResult;
import com.learntv.api.generation.application.port.out.SubtitleFetchPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for episode generation features.
 * Provides endpoints for searching TV shows and managing content generation.
 */
@RestController
@RequestMapping("/api/v1/generation")
@RequiredArgsConstructor
@Validated
@Tag(name = "Generation", description = "Episode content generation")
public class GenerationController {

    private final ShowMetadataPort showMetadataPort;
    private final SubtitleFetchPort subtitleFetchPort;

    @GetMapping("/shows/search")
    @Operation(
            summary = "Search TV shows",
            description = "Search for TV shows by name using TMDB. Returns basic show information including title, poster, and year."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ShowSearchResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search query"
            )
    })
    public ResponseEntity<ShowSearchResponse> searchShows(
            @Parameter(description = "Search query (show name)", example = "The Pitt")
            @RequestParam
            @NotBlank(message = "Search query cannot be blank")
            @Size(min = 2, max = 100, message = "Query must be between 2 and 100 characters")
            String q) {

        List<ShowSearchResult> results = showMetadataPort.searchShows(q);

        List<ShowDto> shows = results.stream()
                .map(this::mapToShowDto)
                .toList();

        return ResponseEntity.ok(new ShowSearchResponse(shows, shows.size()));
    }

    @GetMapping("/shows/{tmdbId}/imdb")
    @Operation(
            summary = "Get IMDB ID",
            description = "Get the IMDB ID for a TV show. Useful for fetching subtitles from OpenSubtitles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IMDB ID found"),
            @ApiResponse(responseCode = "404", description = "Show not found or IMDB ID not available")
    })
    public ResponseEntity<String> getImdbId(
            @Parameter(description = "TMDB show ID", example = "250307")
            @PathVariable int tmdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode) {

        String imdbId = showMetadataPort.getImdbId(String.valueOf(tmdbId), season, episode);

        if (imdbId == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(imdbId);
    }

    @GetMapping("/subtitles/{imdbId}")
    @Operation(
            summary = "Fetch subtitles",
            description = "Fetch subtitles for an episode from OpenSubtitles. Returns the SRT content as plain text."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subtitles found and returned"),
            @ApiResponse(responseCode = "404", description = "No subtitles found for this episode")
    })
    public ResponseEntity<String> fetchSubtitles(
            @Parameter(description = "IMDB ID", example = "tt31938062")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode,
            @Parameter(description = "Language code", example = "en")
            @RequestParam(defaultValue = "en") String language) {

        return subtitleFetchPort.fetchSubtitle(imdbId, season, episode, language)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Maps a port search result to a web DTO.
     */
    private ShowDto mapToShowDto(ShowSearchResult result) {
        return new ShowDto(
                result.tmdbId(),
                null,  // imdbId - requires separate API call
                result.title(),
                result.overview(),
                result.posterPath(),
                result.year(),
                null   // seasons - requires separate API call
        );
    }
}
