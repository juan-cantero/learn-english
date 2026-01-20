package com.learntv.api.catalog.adapter.out.persistence;

import com.learntv.api.catalog.domain.model.*;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "shows")
public class ShowJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccentType accent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    private String imageUrl;

    private int totalSeasons;

    private int totalEpisodes;

    protected ShowJpaEntity() {
    }

    public static ShowJpaEntity fromDomain(Show show) {
        ShowJpaEntity entity = new ShowJpaEntity();
        entity.id = show.getId().value();
        entity.title = show.getTitle();
        entity.slug = show.getSlug();
        entity.description = show.getDescription();
        entity.genre = show.getGenre();
        entity.accent = show.getAccent();
        entity.difficulty = show.getDifficulty();
        entity.imageUrl = show.getImageUrl();
        entity.totalSeasons = show.getTotalSeasons();
        entity.totalEpisodes = show.getTotalEpisodes();
        return entity;
    }

    public Show toDomain() {
        return Show.builder()
                .id(ShowId.of(id))
                .title(title)
                .slug(slug)
                .description(description)
                .genre(genre)
                .accent(accent)
                .difficulty(difficulty)
                .imageUrl(imageUrl)
                .totalSeasons(totalSeasons)
                .totalEpisodes(totalEpisodes)
                .build();
    }

    // Getters for JPA
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public Genre getGenre() {
        return genre;
    }

    public AccentType getAccent() {
        return accent;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getTotalSeasons() {
        return totalSeasons;
    }

    public int getTotalEpisodes() {
        return totalEpisodes;
    }
}
