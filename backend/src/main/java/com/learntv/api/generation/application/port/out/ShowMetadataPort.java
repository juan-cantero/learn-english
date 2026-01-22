package com.learntv.api.generation.application.port.out;

import java.util.List;

public interface ShowMetadataPort {
    List<ShowSearchResult> searchShows(String query);
    String getImdbId(String tmdbId, int season, int episode);

    record ShowSearchResult(String tmdbId, String title, String overview, String posterPath) {}
}
