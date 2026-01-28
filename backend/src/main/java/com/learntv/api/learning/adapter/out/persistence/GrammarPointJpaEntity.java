package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.domain.model.GrammarPoint;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "grammar_points")
public class GrammarPointJpaEntity {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String explanation;

    @Column(length = 500)
    private String structure;

    @Column(length = 1000)
    private String example;

    @Column(length = 1000)
    private String contextQuote;

    protected GrammarPointJpaEntity() {
    }

    public static GrammarPointJpaEntity create(UUID episodeId, String title, String explanation,
                                                String structure, String example) {
        GrammarPointJpaEntity entity = new GrammarPointJpaEntity();
        entity.id = UUID.randomUUID();
        entity.episodeId = episodeId;
        entity.title = title;
        entity.explanation = explanation;
        entity.structure = structure;
        entity.example = example;
        return entity;
    }

    public GrammarPoint toDomain() {
        return GrammarPoint.builder()
                .id(id)
                .episodeId(episodeId)
                .title(title)
                .explanation(explanation)
                .structure(structure)
                .example(example)
                .contextQuote(contextQuote)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }
}
