package com.learntv.api.generation.adapter.in.web;

import com.learntv.api.generation.adapter.in.web.dto.ContentExtractionResponse;
import com.learntv.api.generation.adapter.in.web.dto.ShowDto;
import com.learntv.api.generation.adapter.in.web.dto.ShowSearchResponse;
import com.learntv.api.generation.application.port.out.ContentExtractionPort;
import com.learntv.api.generation.application.port.out.ExerciseGenerationPort;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.generation.application.service.LessonGenerationService;
import com.learntv.api.generation.application.service.LessonGenerationService.GeneratedLessonResult;
import com.learntv.api.generation.application.service.LessonGenerationService.LessonGenerationRequest;
import com.learntv.api.generation.application.port.out.ShowMetadataPort.ShowSearchResult;
import com.learntv.api.generation.application.port.out.SubtitleFetchPort;
import com.learntv.api.generation.application.service.ScriptFetchService;
import com.learntv.api.generation.domain.model.ExtractedExpression;
import com.learntv.api.generation.domain.model.ExtractedGrammar;
import com.learntv.api.generation.domain.model.ExtractedVocabulary;
import com.learntv.api.generation.domain.model.GeneratedExercise;
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
    private final ScriptFetchService scriptFetchService;
    private final ContentExtractionPort contentExtractionPort;
    private final ExerciseGenerationPort exerciseGenerationPort;
    private final LessonGenerationService lessonGenerationService;

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
            summary = "Fetch raw subtitles",
            description = "Fetch raw SRT subtitles for an episode from OpenSubtitles. Returns the SRT content with timestamps."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subtitles found and returned"),
            @ApiResponse(responseCode = "404", description = "No subtitles found for this episode")
    })
    public ResponseEntity<String> fetchSubtitles(
            @Parameter(description = "IMDB ID", example = "tt0903747")
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

    @GetMapping("/scripts/{imdbId}")
    @Operation(
            summary = "Fetch parsed script",
            description = "Fetch clean dialogue text for an episode. Uses caching (30-day TTL) and returns parsed text without timestamps."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Script found and returned"),
            @ApiResponse(responseCode = "404", description = "No script found for this episode")
    })
    public ResponseEntity<String> fetchScript(
            @Parameter(description = "IMDB ID", example = "tt0903747")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode,
            @Parameter(description = "Language code", example = "en")
            @RequestParam(defaultValue = "en") String language) {

        return scriptFetchService.fetchScript(imdbId, season, episode, language)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/extract/vocabulary/{imdbId}")
    @Operation(
            summary = "Extract vocabulary from episode",
            description = "Extract vocabulary items from an episode script using AI. Returns 15-25 vocabulary items with definitions and examples."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vocabulary extracted successfully"),
            @ApiResponse(responseCode = "404", description = "Script not found for this episode")
    })
    public ResponseEntity<ContentExtractionResponse> extractVocabulary(
            @Parameter(description = "IMDB ID", example = "tt0903747")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode,
            @Parameter(description = "Show genre for context", example = "drama")
            @RequestParam(defaultValue = "drama") String genre) {

        return scriptFetchService.fetchScript(imdbId, season, episode)
                .map(script -> {
                    List<ExtractedVocabulary> vocabulary = contentExtractionPort.extractVocabulary(script, genre);
                    return ResponseEntity.ok(ContentExtractionResponse.ofVocabulary(vocabulary));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/extract/grammar/{imdbId}")
    @Operation(
            summary = "Extract grammar points from episode",
            description = "Extract grammar patterns from an episode script using AI. Returns 4-6 grammar points with explanations and examples."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grammar extracted successfully"),
            @ApiResponse(responseCode = "404", description = "Script not found for this episode")
    })
    public ResponseEntity<ContentExtractionResponse> extractGrammar(
            @Parameter(description = "IMDB ID", example = "tt0903747")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode) {

        return scriptFetchService.fetchScript(imdbId, season, episode)
                .map(script -> {
                    List<ExtractedGrammar> grammar = contentExtractionPort.extractGrammar(script);
                    return ResponseEntity.ok(ContentExtractionResponse.ofGrammar(grammar));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/extract/expressions/{imdbId}")
    @Operation(
            summary = "Extract expressions from episode",
            description = "Extract idiomatic expressions and phrases from an episode script using AI. Returns 6-10 expressions with meanings and usage notes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expressions extracted successfully"),
            @ApiResponse(responseCode = "404", description = "Script not found for this episode")
    })
    public ResponseEntity<ContentExtractionResponse> extractExpressions(
            @Parameter(description = "IMDB ID", example = "tt0903747")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode) {

        return scriptFetchService.fetchScript(imdbId, season, episode)
                .map(script -> {
                    List<ExtractedExpression> expressions = contentExtractionPort.extractExpressions(script);
                    return ResponseEntity.ok(ContentExtractionResponse.ofExpressions(expressions));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exercises/{imdbId}")
    @Operation(
            summary = "Generate exercises for episode",
            description = "Generate 12-15 exercises based on extracted vocabulary, grammar, and expressions. " +
                    "Includes fill-in-blank, multiple choice, matching, and listening exercises."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercises generated successfully"),
            @ApiResponse(responseCode = "404", description = "Script not found for this episode")
    })
    public ResponseEntity<List<GeneratedExercise>> generateExercises(
            @Parameter(description = "IMDB ID", example = "tt0903747")
            @PathVariable String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam(defaultValue = "1") int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam(defaultValue = "1") int episode,
            @Parameter(description = "Show genre for context", example = "drama")
            @RequestParam(defaultValue = "drama") String genre) {

        return scriptFetchService.fetchScript(imdbId, season, episode)
                .map(script -> {
                    // Extract content first
                    List<ExtractedVocabulary> vocabulary = contentExtractionPort.extractVocabulary(script, genre);
                    List<ExtractedGrammar> grammar = contentExtractionPort.extractGrammar(script);
                    List<ExtractedExpression> expressions = contentExtractionPort.extractExpressions(script);

                    // Generate exercises from extracted content
                    List<GeneratedExercise> exercises = exerciseGenerationPort.generateExercises(
                            vocabulary, grammar, expressions);

                    return ResponseEntity.ok(exercises);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lessons/create")
    @Operation(
            summary = "Generate and save a complete lesson",
            description = "Generate vocabulary, grammar, expressions, and exercises for an episode and save to database. " +
                    "The lesson will then be viewable in the frontend."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lesson generated and saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Generation failed")
    })
    public ResponseEntity<GeneratedLessonResult> createLesson(
            @Parameter(description = "IMDB ID of the episode", example = "tt0959621")
            @RequestParam String imdbId,
            @Parameter(description = "Season number", example = "1")
            @RequestParam int season,
            @Parameter(description = "Episode number", example = "1")
            @RequestParam int episode,
            @Parameter(description = "Show title", example = "Breaking Bad")
            @RequestParam String showTitle,
            @Parameter(description = "Episode title (optional)", example = "Pilot")
            @RequestParam(required = false) String episodeTitle,
            @Parameter(description = "Show genre", example = "drama")
            @RequestParam(defaultValue = "drama") String genre,
            @Parameter(description = "Show image URL (optional)")
            @RequestParam(required = false) String imageUrl) {

        LessonGenerationRequest request = new LessonGenerationRequest(
                imdbId, season, episode, showTitle, episodeTitle, genre, imageUrl
        );

        GeneratedLessonResult result = lessonGenerationService.generateAndSaveLesson(request);
        return ResponseEntity.ok(result);
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
