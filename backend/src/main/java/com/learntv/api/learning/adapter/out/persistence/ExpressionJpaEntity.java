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

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    protected ExpressionJpaEntity() {
    }

    public static ExpressionJpaEntity create(UUID episodeId, String phrase, String meaning,
                                              String contextQuote, String usageNote, String audioUrl) {
        ExpressionJpaEntity entity = new ExpressionJpaEntity();
        entity.id = UUID.randomUUID();
        entity.episodeId = episodeId;
        entity.phrase = phrase;
        entity.meaning = meaning;
        entity.contextQuote = contextQuote;
        entity.usageNote = usageNote;
        entity.audioUrl = audioUrl;
        return entity;
    }

    public Expression toDomain() {
        return Expression.builder()
                .id(id)
                .episodeId(episodeId)
                .phrase(phrase)
                .meaning(meaning)
                .contextQuote(contextQuote)
                .usageNote(usageNote)
                .audioUrl(audioUrl)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}
