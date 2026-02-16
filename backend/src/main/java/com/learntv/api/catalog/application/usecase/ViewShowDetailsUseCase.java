package com.learntv.api.catalog.application.usecase;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.exception.ShowNotFoundException;
import com.learntv.api.catalog.domain.model.Show;
import com.learntv.api.generation.application.port.out.ShowMetadataPort;
import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.domain.model.Episode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use case: View details of a specific show including its episodes.
 *
 * Orchestrates:
 * - Loading show details from catalog context
 * - Loading episodes from learning context
 * - Combining into a complete show view
 */
public class ViewShowDetailsUseCase {

    private final ShowRepository showRepository;
    private final EpisodeRepository episodeRepository;
    private final ShowMetadataPort showMetadataPort;

    public ViewShowDetailsUseCase(ShowRepository showRepository,
                                   EpisodeRepository episodeRepository,
                                   ShowMetadataPort showMetadataPort) {
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
        this.showMetadataPort = showMetadataPort;
    }

    public ShowWithEpisodes execute(String showSlug) {
        Show show = showRepository.findBySlug(showSlug)
                .orElseThrow(() -> new ShowNotFoundException(showSlug));

        List<Episode> episodes = episodeRepository.findByShowId(show.getId().value());

        return new ShowWithEpisodes(show, episodes);
    }

    /**
     * Get all episodes for a season, cross-referencing TMDB data with generated episodes.
     */
    public List<SeasonEpisodeInfo> getSeasonEpisodes(String showSlug, int seasonNumber) {
        Show show = showRepository.findBySlug(showSlug)
                .orElseThrow(() -> new ShowNotFoundException(showSlug));

        if (show.getTmdbId() == null) {
            // No TMDB ID — return only generated episodes
            return episodeRepository.findByShowId(show.getId().value()).stream()
                    .filter(e -> e.getSeasonNumber() == seasonNumber)
                    .map(e -> new SeasonEpisodeInfo(
                            e.getEpisodeNumber(), e.getTitle(), e.getDurationMinutes(), true, e.getSlug()))
                    .toList();
        }

        // Fetch all TMDB episodes for this season
        var tmdbSeason = showMetadataPort.getSeasonEpisodes(show.getTmdbId(), seasonNumber);
        if (tmdbSeason.isEmpty()) {
            return List.of();
        }

        // Get generated episodes for this show+season and index by episode number
        List<Episode> dbEpisodes = episodeRepository.findByShowId(show.getId().value());
        Map<Integer, Episode> generatedByNumber = dbEpisodes.stream()
                .filter(e -> e.getSeasonNumber() == seasonNumber)
                .collect(Collectors.toMap(Episode::getEpisodeNumber, e -> e, (a, b) -> a));

        // Merge: TMDB episodes with generation status
        return tmdbSeason.get().episodes().stream()
                .map(tmdbEp -> {
                    Episode generated = generatedByNumber.get(tmdbEp.episodeNumber());
                    return new SeasonEpisodeInfo(
                            tmdbEp.episodeNumber(),
                            tmdbEp.title(),
                            tmdbEp.runtime() != null ? tmdbEp.runtime() : 0,
                            generated != null,
                            generated != null ? generated.getSlug() : null
                    );
                })
                .toList();
    }

    /**
     * Episode info combining TMDB metadata with generation status.
     */
    public record SeasonEpisodeInfo(
            int episodeNumber,
            String title,
            int runtime,
            boolean generated,
            String slug
    ) {}

    /**
     * Result combining show and its episodes.
     */
    public record ShowWithEpisodes(
            Show show,
            List<Episode> episodes
    ) {
        public int getEpisodeCount() {
            return episodes.size();
        }

        public List<Episode> getSeasonEpisodes(int seasonNumber) {
            return episodes.stream()
                    .filter(e -> e.getSeasonNumber() == seasonNumber)
                    .toList();
        }

        public int getSeasonCount() {
            return episodes.stream()
                    .mapToInt(Episode::getSeasonNumber)
                    .max()
                    .orElse(0);
        }
    }
}
