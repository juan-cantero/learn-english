package com.learntv.api.generation.adapter.out.tmdb.dto;

import java.util.List;

public record TmdbSearchResponse(
        int page,
        List<TmdbShowResult> results,
        int total_results,
        int total_pages
) {}
