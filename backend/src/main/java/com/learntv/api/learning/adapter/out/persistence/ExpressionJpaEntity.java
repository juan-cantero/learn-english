package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.domain.model.Expression;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "expressions")
public class ExpressionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(nullable = false)
    private String phrase;

    @Column(nullable = false, length = 1000)
    private String meaning;

    @Column(length = 1000)
    private String contextQuote;

    @Column(length = 1000)
    private String usageNote;

    protected ExpressionJpaEntity() {
    }

    public Expression toDomain() {
        return Expression.builder()
                .id(id)
                .episodeId(episodeId)
                .phrase(phrase)
                .meaning(meaning)
                .contextQuote(contextQuote)
                .usageNote(usageNote)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }
}
