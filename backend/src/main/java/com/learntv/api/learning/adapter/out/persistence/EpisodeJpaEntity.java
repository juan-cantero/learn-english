package com.learntv.api.learning.adapter.out.persistence;

import com.learntv.api.learning.domain.model.Episode;
import com.learntv.api.learning.domain.model.EpisodeId;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "episodes")
public class EpisodeJpaEntity {

    @Id
    private UUID id;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Column(name = "show_slug", nullable = false)
    private String showSlug;

    @Column(nullable = false)
    private int seasonNumber;

    @Column(nullable = false)
    private int episodeNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String slug;

    @Column(length = 2000)
    private String synopsis;

    private int durationMinutes;

    protected EpisodeJpaEntity() {
    }

    public static EpisodeJpaEntity create(UUID showId, String showSlug, int seasonNumber,
                                           int episodeNumber, String title, String synopsis) {
        EpisodeJpaEntity entity = new EpisodeJpaEntity();
        entity.id = UUID.randomUUID();
        entity.showId = showId;
        entity.showSlug = showSlug;
        entity.seasonNumber = seasonNumber;
        entity.episodeNumber = episodeNumber;
        entity.title = title;
        entity.slug = showSlug + "-s" + seasonNumber + "e" + episodeNumber;
        entity.synopsis = synopsis;
        entity.durationMinutes = 45; // default
        return entity;
    }

    public static EpisodeJpaEntity fromDomain(Episode episode) {
        EpisodeJpaEntity entity = new EpisodeJpaEntity();
        entity.id = episode.getId().value();
        entity.showId = episode.getShowId();
        entity.showSlug = episode.getShowSlug();
        entity.seasonNumber = episode.getSeasonNumber();
        entity.episodeNumber = episode.getEpisodeNumber();
        entity.title = episode.getTitle();
        entity.slug = episode.getSlug();
        entity.synopsis = episode.getSynopsis();
        entity.durationMinutes = episode.getDurationMinutes();
        return entity;
    }

    public Episode toDomain() {
        return Episode.builder()
                .id(EpisodeId.of(id))
                .showId(showId)
                .showSlug(showSlug)
                .seasonNumber(seasonNumber)
                .episodeNumber(episodeNumber)
                .title(title)
                .slug(slug)
                .synopsis(synopsis)
                .durationMinutes(durationMinutes)
                .build();
    }

    public String getShowSlug() {
        return showSlug;
    }

    public UUID getId() {
        return id;
    }

    public UUID getShowId() {
        return showId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
