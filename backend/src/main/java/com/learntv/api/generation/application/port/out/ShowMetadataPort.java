package com.learntv.api.generation.application.port.out;

import java.util.List;

public interface ShowMetadataPort {
    List<ShowSearchResult> searchShows(String query);
    String getImdbId(String tmdbId, int season, int episode);

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
}
