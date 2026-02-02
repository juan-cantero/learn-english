package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Expression {

    private final UUID id;
    private final UUID episodeId;
    private final String phrase;
    private final String meaning;
    private final String contextQuote;
    private final String usageNote;
    private final String audioUrl;

    private Expression(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.phrase = Objects.requireNonNull(builder.phrase, "phrase is required");
        this.meaning = Objects.requireNonNull(builder.meaning, "meaning is required");
        this.contextQuote = builder.contextQuote;
        this.usageNote = builder.usageNote;
        this.audioUrl = builder.audioUrl;
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

    public String getPhrase() {
        return phrase;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getContextQuote() {
        return contextQuote;
    }

    public String getUsageNote() {
        return usageNote;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public static class Builder {
        private UUID id;
        private UUID episodeId;
        private String phrase;
        private String meaning;
        private String contextQuote;
        private String usageNote;
        private String audioUrl;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder phrase(String phrase) {
            this.phrase = phrase;
            return this;
        }

        public Builder meaning(String meaning) {
            this.meaning = meaning;
            return this;
        }

        public Builder contextQuote(String contextQuote) {
            this.contextQuote = contextQuote;
            return this;
        }

        public Builder usageNote(String usageNote) {
            this.usageNote = usageNote;
            return this;
        }

        public Builder audioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
            return this;
        }

        public Expression build() {
            return new Expression(this);
        }
    }
}
