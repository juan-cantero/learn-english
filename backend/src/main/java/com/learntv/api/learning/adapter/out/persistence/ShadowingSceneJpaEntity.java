package com.learntv.api.learning.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shadowing_scenes")
public class ShadowingSceneJpaEntity {

    @Id
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(name = "scene_index", nullable = false)
    private int sceneIndex;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String lines;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String characters;

    @Column(name = "created_at")
    private Instant createdAt;

    protected ShadowingSceneJpaEntity() {
    }

    public ShadowingSceneJpaEntity(UUID id, UUID episodeId, int sceneIndex,
                                    String title, String lines, String characters) {
        this.id = id;
        this.episodeId = episodeId;
        this.sceneIndex = sceneIndex;
        this.title = title;
        this.lines = lines;
        this.characters = characters;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public int getSceneIndex() {
        return sceneIndex;
    }

    public String getTitle() {
        return title;
    }

    public String getLines() {
        return lines;
    }

    public String getCharacters() {
        return characters;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
