package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.domain.model.Vocabulary;
import com.learntv.api.learning.domain.model.VocabularyCategory;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "vocabulary")
public class VocabularyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false, length = 1000)
    private String definition;

    private String phonetic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VocabularyCategory category;

    @Column(length = 1000)
    private String exampleSentence;

    private String contextTimestamp;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    protected VocabularyJpaEntity() {
    }

    public static VocabularyJpaEntity create(UUID episodeId, String term, String definition,
                                              String phonetic, VocabularyCategory category,
                                              String exampleSentence, String audioUrl) {
        VocabularyJpaEntity entity = new VocabularyJpaEntity();
        entity.id = UUID.randomUUID();
        entity.episodeId = episodeId;
        entity.term = term;
        entity.definition = definition;
        entity.phonetic = phonetic;
        entity.category = category;
        entity.exampleSentence = exampleSentence;
        entity.audioUrl = audioUrl;
        return entity;
    }

    public Vocabulary toDomain() {
        return Vocabulary.builder()
                .id(id)
                .episodeId(episodeId)
                .term(term)
                .definition(definition)
                .phonetic(phonetic)
                .category(category)
                .exampleSentence(exampleSentence)
                .contextTimestamp(contextTimestamp)
                .audioUrl(audioUrl)
                .build();
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

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
