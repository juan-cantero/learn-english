package com.learntv.api.catalog.domain.model;

import java.util.Objects;

public class Show {

    private final ShowId id;
    private final String title;
    private final String slug;
    private final String description;
    private final Genre genre;
    private final AccentType accent;
    private final DifficultyLevel difficulty;
    private final String imageUrl;
    private final int totalSeasons;
    private final int totalEpisodes;

    private Show(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.title = Objects.requireNonNull(builder.title, "title is required");
        this.slug = Objects.requireNonNull(builder.slug, "slug is required");
        this.description = builder.description;
        this.genre = Objects.requireNonNull(builder.genre, "genre is required");
        this.accent = Objects.requireNonNull(builder.accent, "accent is required");
        this.difficulty = Objects.requireNonNull(builder.difficulty, "difficulty is required");
        this.imageUrl = builder.imageUrl;
        this.totalSeasons = builder.totalSeasons;
        this.totalEpisodes = builder.totalEpisodes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ShowId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Genre getGenre() {
        return genre;
    }

    public AccentType getAccent() {
        return accent;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }

    public int getTotalEpisodes() {
        return totalEpisodes;
    }

    public static class Builder {
        private ShowId id;
        private String title;
        private String slug;
        private String description;
        private Genre genre;
        private AccentType accent;
        private DifficultyLevel difficulty;
        private String imageUrl;
        private int totalSeasons;
        private int totalEpisodes;

        public Builder id(ShowId id) {
            this.id = id;
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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder genre(Genre genre) {
            this.genre = genre;
            return this;
        }

        public Builder accent(AccentType accent) {
            this.accent = accent;
            return this;
        }

        public Builder difficulty(DifficultyLevel difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder totalSeasons(int totalSeasons) {
            this.totalSeasons = totalSeasons;
            return this;
        }

        public Builder totalEpisodes(int totalEpisodes) {
            this.totalEpisodes = totalEpisodes;
            return this;
        }

        public Show build() {
            return new Show(this);
        }
    }
}
