package com.learntv.api.generation.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Permanently stored script for an episode.
 * Scripts are fetched once from OpenSubtitles and stored forever.
 * Used as source of truth for content generation and regeneration.
 */
@Entity
@Table(name = "episode_scripts")
public class EpisodeScriptEntity {

    @Id
    private UUID id;

    @Column(name = "imdb_id", nullable = false, length = 20)
    private String imdbId;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(name = "episode_number", nullable = false)
    private int episodeNumber;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "raw_content", nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Column(name = "parsed_text", nullable = false, columnDefinition = "TEXT")
    private String parsedText;

    @Column(name = "downloaded_at", nullable = false)
    private Instant downloadedAt;

    protected EpisodeScriptEntity() {
    }

    public EpisodeScriptEntity(UUID id, String imdbId, int seasonNumber, int episodeNumber,
                                String language, String rawContent, String parsedText,
                                Instant downloadedAt) {
        this.id = id;
        this.imdbId = imdbId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.language = language;
        this.rawContent = rawContent;
        this.parsedText = parsedText;
        this.downloadedAt = downloadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getImdbId() {
        return imdbId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getLanguage() {
        return language;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getParsedText() {
        return parsedText;
    }

    public Instant getDownloadedAt() {
        return downloadedAt;
    }
}
