package com.learntv.api.progress.adapter.out.persistence;

import com.learntv.api.progress.domain.model.UserProgress;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "episode_id"})
})
public class UserProgressJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    private int vocabularyScore;

    private int grammarScore;

    private int expressionsScore;

    private int exercisesScore;

    private int totalPoints;

    private boolean completed;

    private Instant lastAccessed;

    protected UserProgressJpaEntity() {
    }

    public static UserProgressJpaEntity fromDomain(UserProgress progress) {
        UserProgressJpaEntity entity = new UserProgressJpaEntity();
        entity.id = progress.getId();
        entity.userId = progress.getUserId();
        entity.episodeId = progress.getEpisodeId();
        entity.vocabularyScore = progress.getVocabularyScore();
        entity.grammarScore = progress.getGrammarScore();
        entity.expressionsScore = progress.getExpressionsScore();
        entity.exercisesScore = progress.getExercisesScore();
        entity.totalPoints = progress.getTotalPoints();
        entity.completed = progress.isCompleted();
        entity.lastAccessed = progress.getLastAccessed();
        return entity;
    }

    public UserProgress toDomain() {
        return UserProgress.builder()
                .id(id)
                .userId(userId)
                .episodeId(episodeId)
                .vocabularyScore(vocabularyScore)
                .grammarScore(grammarScore)
                .expressionsScore(expressionsScore)
                .exercisesScore(exercisesScore)
                .totalPoints(totalPoints)
                .completed(completed)
                .lastAccessed(lastAccessed)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }
}
