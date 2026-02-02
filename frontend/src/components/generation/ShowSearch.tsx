import { useState, useEffect } from 'react';
import { useSearchShows } from '../../hooks/useGeneration';
import { SearchShowCard } from './SearchShowCard';
import { EmptyState } from '../shared/EmptyState';

const POPULAR_SHOWS = [
  { name: 'Friends', query: 'friends' },
  { name: 'Breaking Bad', query: 'breaking bad' },
  { name: 'The Office', query: 'the office' },
  { name: 'Game of Thrones', query: 'game of thrones' },
  { name: 'The Simpsons', query: 'simpsons' },
  { name: 'Stranger Things', query: 'stranger things' },
];

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

  const handlePopularClick = (showQuery: string) => {
    setQuery(showQuery);
  };

  const hasSearched = debouncedQuery.length >= 2;
  const showInitialState = !hasSearched && !isLoading;
  const showNoResults = hasSearched && shows && shows.length === 0;

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
        <EmptyState
          icon="connection"
          title="Connection Error"
          description={error.message}
          action={{
            label: 'Try Again',
            onClick: () => setQuery(query + ' '), // Trigger re-fetch
          }}
        />
      )}

      {showNoResults && (
        <EmptyState
          icon="search"
          title="No shows found"
          description={`We couldn't find any shows matching "${debouncedQuery}". Try a different search term.`}
        >
          <div className="mt-6">
            <p className="mb-3 text-sm text-text-secondary">Popular shows:</p>
            <div className="flex flex-wrap justify-center gap-2">
              {POPULAR_SHOWS.slice(0, 4).map((show) => (
                <button
                  key={show.query}
                  onClick={() => handlePopularClick(show.query)}
                  className="rounded-full border border-border bg-bg-card px-3 py-1.5 text-sm text-text-secondary transition-colors hover:border-accent-primary hover:text-accent-primary"
                >
                  {show.name}
                </button>
              ))}
            </div>
          </div>
        </EmptyState>
      )}

      {showInitialState && (
        <EmptyState
          icon="tv"
          title="Start Learning"
          description="Search for your favorite TV show to create an English lesson from real dialogue."
        >
          <div className="mt-6 w-full max-w-md">
            <p className="mb-3 text-sm text-text-secondary">Popular shows:</p>
            <div className="flex flex-wrap justify-center gap-2">
              {POPULAR_SHOWS.map((show) => (
                <button
                  key={show.query}
                  onClick={() => handlePopularClick(show.query)}
                  className="rounded-full border border-border bg-bg-card px-3 py-1.5 text-sm text-text-secondary transition-colors hover:border-accent-primary hover:text-accent-primary"
                >
                  {show.name}
                </button>
              ))}
            </div>
          </div>
        </EmptyState>
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
