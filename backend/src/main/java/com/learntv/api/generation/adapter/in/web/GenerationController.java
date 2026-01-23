package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for episode generation features.
 * Temporary test endpoints - will be expanded in task 2.2.
 */
@RestController
@RequestMapping("/api/v1/generation")
@RequiredArgsConstructor
@Tag(name = "Generation", description = "Episode content generation")
public class GenerationController {

    private final ShowMetadataPort showMetadataPort;

    @GetMapping("/shows/search")
    @Operation(summary = "Search TV shows", description = "Search for TV shows by name using TMDB")
    public Object searchShows(@RequestParam String q) {
        return showMetadataPort.searchShows(q);
    }

    @GetMapping("/shows/{tmdbId}/imdb")
    @Operation(summary = "Get IMDB ID", description = "Get the IMDB ID for a show")
    public String getImdbId(
            @PathVariable int tmdbId,
            @RequestParam(defaultValue = "1") int season,
            @RequestParam(defaultValue = "1") int episode) {
        return showMetadataPort.getImdbId(String.valueOf(tmdbId), season, episode);
    }
}
