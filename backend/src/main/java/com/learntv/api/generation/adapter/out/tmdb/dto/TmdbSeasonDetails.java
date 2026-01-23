package com.learntv.api.generation.adapter.out.tmdb.dto;

import java.util.List;

public record TmdbSeasonDetails(
        int id,
        int season_number,
        List<TmdbEpisode> episodes
) {}
