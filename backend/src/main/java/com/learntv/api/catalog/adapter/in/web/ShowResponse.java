package com.learntv.api.catalog.adapter.in.web;

import com.learntv.api.catalog.domain.model.Show;

public record ShowResponse(
        String id,
        String title,
        String slug,
        String description,
        String genre,
        String accent,
        String difficulty,
        String imageUrl,
        int totalSeasons,
        int totalEpisodes
) {

    public static ShowResponse fromDomain(Show show) {
        return new ShowResponse(
                show.getId().toString(),
                show.getTitle(),
                show.getSlug(),
                show.getDescription(),
                show.getGenre().name(),
                show.getAccent().name(),
                show.getDifficulty().name(),
                show.getImageUrl(),
                show.getTotalSeasons(),
                show.getTotalEpisodes()
        );
    }
}
