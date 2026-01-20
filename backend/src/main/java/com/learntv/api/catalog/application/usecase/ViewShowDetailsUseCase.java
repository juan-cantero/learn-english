package com.learntv.api.catalog.application.usecase;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.exception.ShowNotFoundException;
import com.learntv.api.catalog.domain.model.Show;
import com.learntv.api.learning.application.port.EpisodeRepository;
import com.learntv.api.learning.domain.model.Episode;

import java.util.List;

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

    public ViewShowDetailsUseCase(ShowRepository showRepository,
                                   EpisodeRepository episodeRepository) {
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
    }

    public ShowWithEpisodes execute(String showSlug) {
        Show show = showRepository.findBySlug(showSlug)
                .orElseThrow(() -> new ShowNotFoundException(showSlug));

        List<Episode> episodes = episodeRepository.findByShowId(show.getId().value());

        return new ShowWithEpisodes(show, episodes);
    }

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
