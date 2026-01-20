package com.learntv.api.catalog.adapter.in.web;

import com.learntv.api.catalog.application.usecase.BrowseCatalogUseCase;
import com.learntv.api.catalog.application.usecase.ViewShowDetailsUseCase;
import com.learntv.api.catalog.domain.model.DifficultyLevel;
import com.learntv.api.catalog.domain.model.Genre;
import com.learntv.api.catalog.domain.model.Show;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@Tag(name = "Shows", description = "TV Show catalog operations")
public class ShowController {

    private final BrowseCatalogUseCase browseCatalogUseCase;
    private final ViewShowDetailsUseCase viewShowDetailsUseCase;

    public ShowController(BrowseCatalogUseCase browseCatalogUseCase,
                          ViewShowDetailsUseCase viewShowDetailsUseCase) {
        this.browseCatalogUseCase = browseCatalogUseCase;
        this.viewShowDetailsUseCase = viewShowDetailsUseCase;
    }

    @GetMapping
    @Operation(summary = "Browse catalog", description = "Returns all available TV shows, optionally filtered")
    public ResponseEntity<List<ShowResponse>> browseShows(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String search) {

        BrowseCatalogUseCase.CatalogFilter filter = new BrowseCatalogUseCase.CatalogFilter(
                genre != null ? Genre.valueOf(genre.toUpperCase()) : null,
                difficulty != null ? DifficultyLevel.valueOf(difficulty.toUpperCase()) : null,
                search
        );

        List<Show> shows = browseCatalogUseCase.execute(filter);
        List<ShowResponse> response = shows.stream()
                .map(ShowResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "View show details", description = "Returns show details with list of episodes")
    public ResponseEntity<ShowWithEpisodesResponse> getShowBySlug(@PathVariable String slug) {
        ViewShowDetailsUseCase.ShowWithEpisodes result = viewShowDetailsUseCase.execute(slug);
        return ResponseEntity.ok(ShowWithEpisodesResponse.fromDomain(result));
    }
}
