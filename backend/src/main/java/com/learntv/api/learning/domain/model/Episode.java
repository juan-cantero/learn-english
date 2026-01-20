package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Episode {

    private final EpisodeId id;
    private final UUID showId;
    private final String showSlug;  // Denormalized to avoid cross-context joins
    private final int seasonNumber;
    private final int episodeNumber;
    private final String title;
    private final String slug;
    private final String synopsis;
    private final int durationMinutes;

    private Episode(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.showId = Objects.requireNonNull(builder.showId, "showId is required");
        this.showSlug = Objects.requireNonNull(builder.showSlug, "showSlug is required");
        this.seasonNumber = builder.seasonNumber;
        this.episodeNumber = builder.episodeNumber;
        this.title = Objects.requireNonNull(builder.title, "title is required");
        this.slug = Objects.requireNonNull(builder.slug, "slug is required");
        this.synopsis = builder.synopsis;
        this.durationMinutes = builder.durationMinutes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public EpisodeId getId() {
        return id;
    }

    public UUID getShowId() {
        return showId;
    }

    public String getShowSlug() {
        return showSlug;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public static class Builder {
        private EpisodeId id;
        private UUID showId;
        private String showSlug;
        private int seasonNumber;
        private int episodeNumber;
        private String title;
        private String slug;
        private String synopsis;
        private int durationMinutes;

        public Builder id(EpisodeId id) {
            this.id = id;
            return this;
        }

        public Builder showId(UUID showId) {
            this.showId = showId;
            return this;
        }

        public Builder showSlug(String showSlug) {
            this.showSlug = showSlug;
            return this;
        }

        public Builder seasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public Builder episodeNumber(int episodeNumber) {
            this.episodeNumber = episodeNumber;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public Builder synopsis(String synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Builder durationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Episode build() {
            return new Episode(this);
        }
    }
}
