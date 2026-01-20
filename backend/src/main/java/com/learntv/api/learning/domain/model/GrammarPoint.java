package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

public class GrammarPoint {

    private final UUID id;
    private final UUID episodeId;
    private final String title;
    private final String explanation;
    private final String structure;
    private final String example;
    private final String contextQuote;

    private GrammarPoint(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.title = Objects.requireNonNull(builder.title, "title is required");
        this.explanation = Objects.requireNonNull(builder.explanation, "explanation is required");
        this.structure = builder.structure;
        this.example = builder.example;
        this.contextQuote = builder.contextQuote;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public String getTitle() {
        return title;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getStructure() {
        return structure;
    }

    public String getExample() {
        return example;
    }

    public String getContextQuote() {
        return contextQuote;
    }

    public static class Builder {
        private UUID id;
        private UUID episodeId;
        private String title;
        private String explanation;
        private String structure;
        private String example;
        private String contextQuote;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public Builder structure(String structure) {
            this.structure = structure;
            return this;
        }

        public Builder example(String example) {
            this.example = example;
            return this;
        }

        public Builder contextQuote(String contextQuote) {
            this.contextQuote = contextQuote;
            return this;
        }

        public GrammarPoint build() {
            return new GrammarPoint(this);
        }
    }
}
