package com.learntv.api.generation.adapter.out.tmdb.dto;

public record TmdbSeason(
        int id,
        int season_number,
        String name,
        int episode_count
) {}
