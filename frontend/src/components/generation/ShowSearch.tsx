import { useState, useEffect } from 'react';
import { useSearchShows } from '../../hooks/useGeneration';
import { SearchShowCard } from './SearchShowCard';

export function ShowSearch() {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  // Debounce search query
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query);
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  const { data: shows, isLoading, error } = useSearchShows(debouncedQuery);

  return (
    <div className="space-y-6">
      <div>
        <label htmlFor="show-search" className="mb-2 block text-sm font-medium text-text-primary">
          Search for a TV Show
        </label>
        <input
          id="show-search"
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Type show name..."
          className="w-full rounded-lg border border-border bg-bg-card px-4 py-3 text-text-primary placeholder-text-secondary transition-colors focus:border-accent-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/50"
        />
        <p className="mt-2 text-xs text-text-secondary">
          Search The Movie Database for TV shows
        </p>
      </div>

      {query.length > 0 && query.length < 2 && (
        <div className="rounded-lg border border-border bg-bg-card p-4 text-center">
          <p className="text-sm text-text-secondary">Type at least 2 characters to search</p>
        </div>
      )}

      {isLoading && (
        <div className="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3 lg:grid-cols-4">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="aspect-[2/3] rounded-t-xl bg-bg-card" />
              <div className="rounded-b-xl bg-bg-card p-3 sm:p-4">
                <div className="mb-2 h-4 w-3/4 rounded bg-bg-dark sm:h-5" />
                <div className="mb-2 h-3 w-1/4 rounded bg-bg-dark" />
                <div className="hidden h-4 w-full rounded bg-bg-dark sm:block" />
                <div className="mt-1 hidden h-4 w-5/6 rounded bg-bg-dark sm:block" />
              </div>
            </div>
          ))}
        </div>
      )}

      {error && (
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to search shows: {error.message}</p>
        </div>
      )}

      {shows && shows.length === 0 && debouncedQuery.length >= 2 && (
        <div className="rounded-lg border border-border bg-bg-card p-8 text-center">
          <p className="text-text-secondary">No shows found for "{debouncedQuery}"</p>
        </div>
      )}

      {shows && shows.length > 0 && (
        <div className="grid grid-cols-2 gap-3 sm:gap-4 md:grid-cols-3 lg:grid-cols-4">
          {shows.map((show) => (
            <SearchShowCard key={show.tmdbId} show={show} />
          ))}
        </div>
      )}
    </div>
  );
}
