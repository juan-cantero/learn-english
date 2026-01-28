package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.domain.model.Exercise;
import com.learntv.api.learning.domain.model.ExerciseType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "exercises")
public class ExerciseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseType type;

    @Column(nullable = false, length = 1000)
    private String question;

    private String correctAnswer;

    @Column(length = 2000)
    private String options;

    @Column(length = 2000)
    private String matchingPairs;

    private int points;

    protected ExerciseJpaEntity() {
    }

    public static ExerciseJpaEntity create(UUID episodeId, ExerciseType type, String question,
                                            String correctAnswer, String options, int points) {
        ExerciseJpaEntity entity = new ExerciseJpaEntity();
        entity.id = UUID.randomUUID();
        entity.episodeId = episodeId;
        entity.type = type;
        entity.question = question;
        entity.correctAnswer = correctAnswer;
        entity.options = options;
        entity.points = points;
        return entity;
    }

    public Exercise toDomain() {
        return Exercise.builder()
                .id(id)
                .episodeId(episodeId)
                .type(type)
                .question(question)
                .correctAnswer(correctAnswer)
                .options(options)
                .matchingPairs(matchingPairs)
                .points(points)
                .build();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }
}
