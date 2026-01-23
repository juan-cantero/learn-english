package com.learntv.api.generation.adapter.out.tmdb.dto;

import java.util.List;

public record TmdbShowDetails(
        int id,
        String name,
        String overview,
        String poster_path,
        int number_of_seasons,
        int number_of_episodes,
        TmdbExternalIds external_ids,
        List<TmdbSeason> seasons
) {}
