package com.learntv.api.generation.adapter.out.tmdb.dto;

public record TmdbEpisode(
        int id,
        int episode_number,
        String name,
        String overview,
        int runtime
) {}
