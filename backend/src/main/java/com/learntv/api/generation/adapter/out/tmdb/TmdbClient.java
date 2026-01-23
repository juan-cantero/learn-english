package com.learntv.api.generation.adapter.out.tmdb;

import com.learntv.api.generation.adapter.out.tmdb.dto.TmdbSearchResponse;
import com.learntv.api.generation.adapter.out.tmdb.dto.TmdbSeasonDetails;
import com.learntv.api.generation.adapter.out.tmdb.dto.TmdbShowDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

    private final WebClient tmdbWebClient;
    private final String apiKey;

    public TmdbClient(WebClient tmdbWebClient, TmdbConfig tmdbConfig) {
        this.tmdbWebClient = tmdbWebClient;
        this.apiKey = tmdbConfig.getApiKey();
    }

    /**
     * Search for TV shows by name.
     *
     * @param query the search query
     * @return the search response containing results, or empty if an error occurs
     */
    public Optional<TmdbSearchResponse> searchShows(String query) {
        log.debug("Searching TMDB for shows with query: {}", query);

        try {
            TmdbSearchResponse response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/tv")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", query)
                            .queryParam("include_adult", false)
                            .queryParam("language", "en-US")
                            .queryParam("page", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbSearchResponse.class)
                    .block();

            log.debug("TMDB search returned {} results",
                    response != null ? response.total_results() : 0);

            return Optional.ofNullable(response);
        } catch (WebClientResponseException e) {
            log.error("TMDB API error during search: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error searching TMDB for shows: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get detailed information about a TV show, including external IDs.
     *
     * @param tmdbId the TMDB ID of the show
     * @return the show details, or empty if not found or an error occurs
     */
    public Optional<TmdbShowDetails> getShowDetails(int tmdbId) {
        log.debug("Fetching TMDB show details for ID: {}", tmdbId);

        try {
            TmdbShowDetails response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tv/{id}")
                            .queryParam("api_key", apiKey)
                            .queryParam("append_to_response", "external_ids")
                            .queryParam("language", "en-US")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(TmdbShowDetails.class)
                    .block();

            log.debug("TMDB show details retrieved for: {}",
                    response != null ? response.name() : "unknown");

            return Optional.ofNullable(response);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Show not found in TMDB with ID: {}", tmdbId);
            return Optional.empty();
        } catch (WebClientResponseException e) {
            log.error("TMDB API error fetching show details: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching show details from TMDB: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get detailed information about a season, including all episodes.
     *
     * @param tmdbId       the TMDB ID of the show
     * @param seasonNumber the season number
     * @return the season details, or empty if not found or an error occurs
     */
    public Optional<TmdbSeasonDetails> getSeasonDetails(int tmdbId, int seasonNumber) {
        log.debug("Fetching TMDB season details for show {} season {}", tmdbId, seasonNumber);

        try {
            TmdbSeasonDetails response = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tv/{id}/season/{season_number}")
                            .queryParam("api_key", apiKey)
                            .queryParam("language", "en-US")
                            .build(tmdbId, seasonNumber))
                    .retrieve()
                    .bodyToMono(TmdbSeasonDetails.class)
                    .block();

            log.debug("TMDB season details retrieved with {} episodes",
                    response != null && response.episodes() != null
                            ? response.episodes().size() : 0);

            return Optional.ofNullable(response);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Season not found in TMDB: show {} season {}", tmdbId, seasonNumber);
            return Optional.empty();
        } catch (WebClientResponseException e) {
            log.error("TMDB API error fetching season details: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching season details from TMDB: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
