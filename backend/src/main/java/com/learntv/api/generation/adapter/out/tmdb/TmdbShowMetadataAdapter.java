package com.learntv.api.generation.adapter.out.tmdb;

import com.learntv.api.generation.adapter.out.tmdb.dto.TmdbShowDetails;
import com.learntv.api.generation.adapter.out.tmdb.dto.TmdbShowResult;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class TmdbShowMetadataAdapter implements ShowMetadataPort {

    private static final Logger log = LoggerFactory.getLogger(TmdbShowMetadataAdapter.class);
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private final TmdbClient tmdbClient;

    public TmdbShowMetadataAdapter(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    @Override
    public List<ShowSearchResult> searchShows(String query) {
        log.debug("Searching for shows with query: {}", query);

        return tmdbClient.searchShows(query)
                .map(response -> response.results() != null
                        ? response.results().stream()
                                .map(this::mapToShowSearchResult)
                                .toList()
                        : Collections.<ShowSearchResult>emptyList())
                .orElse(Collections.emptyList());
    }

    @Override
    public String getImdbId(String tmdbId, int season, int episode) {
        log.debug("Getting IMDB ID for TMDB ID: {}, season: {}, episode: {}",
                tmdbId, season, episode);

        try {
            int tmdbIdInt = Integer.parseInt(tmdbId);

            Optional<TmdbShowDetails> showDetails = tmdbClient.getShowDetails(tmdbIdInt);

            if (showDetails.isEmpty()) {
                log.warn("Could not find show details for TMDB ID: {}", tmdbId);
                return null;
            }

            TmdbShowDetails details = showDetails.get();

            if (details.external_ids() != null && details.external_ids().imdb_id() != null) {
                String imdbId = details.external_ids().imdb_id();
                log.debug("Found IMDB ID: {} for TMDB ID: {}", imdbId, tmdbId);
                return imdbId;
            }

            log.warn("No IMDB ID found in external IDs for TMDB ID: {}", tmdbId);
            return null;

        } catch (NumberFormatException e) {
            log.error("Invalid TMDB ID format: {}", tmdbId);
            return null;
        }
    }

    /**
     * Maps a TMDB show result to a domain ShowSearchResult.
     *
     * @param tmdbResult the TMDB API result
     * @return the domain search result
     */
    private ShowSearchResult mapToShowSearchResult(TmdbShowResult tmdbResult) {
        String posterPath = tmdbResult.poster_path() != null
                ? TMDB_IMAGE_BASE_URL + tmdbResult.poster_path()
                : null;

        Integer year = extractYear(tmdbResult.first_air_date());

        return new ShowSearchResult(
                String.valueOf(tmdbResult.id()),
                tmdbResult.name(),
                tmdbResult.overview(),
                posterPath,
                year
        );
    }

    /**
     * Extracts the year from a date string in format "YYYY-MM-DD".
     *
     * @param dateString the date string
     * @return the year, or null if parsing fails
     */
    private Integer extractYear(String dateString) {
        if (dateString == null || dateString.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(dateString.substring(0, 4));
        } catch (NumberFormatException e) {
            log.warn("Could not parse year from date: {}", dateString);
            return null;
        }
    }
}
