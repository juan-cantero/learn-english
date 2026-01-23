package com.learntv.api.generation.adapter.out.tmdb.dto;

public record TmdbShowResult(
        int id,
        String name,
        String overview,
        String poster_path,
        String first_air_date,
        double vote_average
) {}
