package com.learntv.api.learning.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Vocabulary {

    private final UUID id;
    private final UUID episodeId;
    private final String term;
    private final String definition;
    private final String phonetic;
    private final VocabularyCategory category;
    private final String exampleSentence;
    private final String contextTimestamp;

    private Vocabulary(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.episodeId = Objects.requireNonNull(builder.episodeId, "episodeId is required");
        this.term = Objects.requireNonNull(builder.term, "term is required");
        this.definition = Objects.requireNonNull(builder.definition, "definition is required");
        this.phonetic = builder.phonetic;
        this.category = Objects.requireNonNull(builder.category, "category is required");
        this.exampleSentence = builder.exampleSentence;
        this.contextTimestamp = builder.contextTimestamp;
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

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public VocabularyCategory getCategory() {
        return category;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public String getContextTimestamp() {
        return contextTimestamp;
    }

    public static class Builder {
        private UUID id;
        private UUID episodeId;
        private String term;
        private String definition;
        private String phonetic;
        private VocabularyCategory category;
        private String exampleSentence;
        private String contextTimestamp;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder episodeId(UUID episodeId) {
            this.episodeId = episodeId;
            return this;
        }

        public Builder term(String term) {
            this.term = term;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder phonetic(String phonetic) {
            this.phonetic = phonetic;
            return this;
        }

        public Builder category(VocabularyCategory category) {
            this.category = category;
            return this;
        }

        public Builder exampleSentence(String exampleSentence) {
            this.exampleSentence = exampleSentence;
            return this;
        }

        public Builder contextTimestamp(String contextTimestamp) {
            this.contextTimestamp = contextTimestamp;
            return this;
        }

        public Vocabulary build() {
            return new Vocabulary(this);
        }
    }
}
