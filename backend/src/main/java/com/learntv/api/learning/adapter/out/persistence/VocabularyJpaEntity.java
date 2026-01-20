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

    protected VocabularyJpaEntity() {
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
                .build();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }
}
