package com.learntv.api.catalog.application.usecase;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.domain.model.DifficultyLevel;
import com.learntv.api.catalog.domain.model.Genre;
import com.learntv.api.catalog.domain.model.Show;

import java.util.List;

/**
 * Use case: Browse the TV show catalog.
 *
 * Supports filtering by genre and difficulty level.
 * Returns shows sorted by title.
 */
public class BrowseCatalogUseCase {

    private final ShowRepository showRepository;

    public BrowseCatalogUseCase(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    /**
     * Get all shows in the catalog.
     */
    public List<Show> execute() {
        return showRepository.findAll();
    }

    /**
     * Get shows filtered by criteria.
     */
    public List<Show> execute(CatalogFilter filter) {
        List<Show> shows = showRepository.findAll();

        return shows.stream()
                .filter(show -> matchesFilter(show, filter))
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .toList();
    }

    private boolean matchesFilter(Show show, CatalogFilter filter) {
        if (filter == null) return true;

        if (filter.genre() != null && show.getGenre() != filter.genre()) {
            return false;
        }

        if (filter.difficulty() != null && show.getDifficulty() != filter.difficulty()) {
            return false;
        }

        if (filter.searchTerm() != null && !filter.searchTerm().isBlank()) {
            String term = filter.searchTerm().toLowerCase();
            return show.getTitle().toLowerCase().contains(term) ||
                   (show.getDescription() != null && show.getDescription().toLowerCase().contains(term));
        }

        return true;
    }

    /**
     * Filter criteria for browsing catalog.
     */
    public record CatalogFilter(
            Genre genre,
            DifficultyLevel difficulty,
            String searchTerm
    ) {
        public static CatalogFilter none() {
            return new CatalogFilter(null, null, null);
        }

        public static CatalogFilter byGenre(Genre genre) {
            return new CatalogFilter(genre, null, null);
        }

        public static CatalogFilter byDifficulty(DifficultyLevel difficulty) {
            return new CatalogFilter(null, difficulty, null);
        }

        public static CatalogFilter bySearch(String term) {
            return new CatalogFilter(null, null, term);
        }
    }
}
