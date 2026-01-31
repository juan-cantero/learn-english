package com.learntv.api.generation.application.port.out;

import java.util.List;
import java.util.Optional;

public interface ShowMetadataPort {
    List<ShowSearchResult> searchShows(String query);
    String getImdbId(String tmdbId, int season, int episode);

    /**
     * Get show details including all seasons.
     *
     * @param tmdbId TMDB identifier
     * @return show with seasons, or empty if not found
     */
    Optional<ShowWithSeasons> getShowWithSeasons(String tmdbId);

    /**
     * Get all episodes for a specific season.
     *
     * @param tmdbId       TMDB identifier
     * @param seasonNumber season number
     * @return season with episodes, or empty if not found
     */
    Optional<SeasonWithEpisodes> getSeasonEpisodes(String tmdbId, int seasonNumber);

    /**
     * Result from searching for TV shows.
     *
     * @param tmdbId     TMDB identifier
     * @param title      Show title
     * @param overview   Show description/overview
     * @param posterPath Full URL to poster image
     * @param year       First air year (null if not available)
     */
    record ShowSearchResult(
            String tmdbId,
            String title,
            String overview,
            String posterPath,
            Integer year
    ) {}

    /**
     * Show with its seasons.
     */
    record ShowWithSeasons(
            String tmdbId,
            String title,
            String overview,
            String posterUrl,
            Integer year,
            List<Season> seasons
    ) {}

    /**
     * Season summary.
     */
    record Season(
            int seasonNumber,
            String name,
            int episodeCount
    ) {}

    /**
     * Season with all its episodes.
     */
    record SeasonWithEpisodes(
            String tmdbId,
            String showTitle,
            int seasonNumber,
            List<Episode> episodes
    ) {}

    /**
     * Episode details.
     */
    record Episode(
            int episodeNumber,
            String title,
            String overview,
            Integer runtime
    ) {}
}
