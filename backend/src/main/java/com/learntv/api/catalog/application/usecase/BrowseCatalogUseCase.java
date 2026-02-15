package com.learntv.api.catalog.application.usecase;

import com.learntv.api.catalog.application.port.ShowRepository;
import com.learntv.api.catalog.application.port.UserShowRepository;
import com.learntv.api.catalog.domain.model.DifficultyLevel;
import com.learntv.api.catalog.domain.model.Genre;
import com.learntv.api.catalog.domain.model.Show;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: Browse the TV show catalog.
 *
 * Supports filtering by genre and difficulty level.
 * Returns shows sorted by title.
 */
public class BrowseCatalogUseCase {

    private final ShowRepository showRepository;
    private final UserShowRepository userShowRepository;

    public BrowseCatalogUseCase(ShowRepository showRepository, UserShowRepository userShowRepository) {
        this.showRepository = showRepository;
        this.userShowRepository = userShowRepository;
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

    /**
     * Get shows filtered by criteria for a specific user.
     * Only returns shows that the user has access to.
     */
    public List<Show> execute(CatalogFilter filter, UUID userId) {
        // Fetch show IDs that the user has access to
        List<UUID> userShowIds = userShowRepository.findShowIdsByUserId(userId);
        Set<UUID> userShowIdSet = Set.copyOf(userShowIds);

        // Get all shows and filter by user access
        List<Show> shows = showRepository.findAll();

        return shows.stream()
                .filter(show -> userShowIdSet.contains(show.getId().value()))
                .filter(show -> matchesFilter(show, filter))
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .toList();
    }

    private static final int PUBLIC_PREVIEW_LIMIT = 5;

    /**
     * Get a public preview of the catalog (for unauthenticated users).
     * Returns up to 5 shows, sorted by title.
     */
    public List<Show> executePublicPreview(CatalogFilter filter) {
        List<Show> shows = showRepository.findAll();

        return shows.stream()
                .filter(show -> matchesFilter(show, filter))
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .limit(PUBLIC_PREVIEW_LIMIT)
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
