import { Link } from '@tanstack/react-router';
import { useShows } from '../hooks/useShows';
import { ShowCard } from '../components/show/ShowCard';
import { EmptyState } from '../components/shared/EmptyState';

export function HomePage() {
  const { data: shows, isLoading, error, refetch } = useShows();

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-text-primary">
          Learn English with TV Shows
        </h1>
        <p className="mt-2 text-text-secondary">
          Choose a show to start learning vocabulary, grammar, and expressions from real dialogue.
        </p>
      </div>

      {isLoading && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="aspect-video rounded-t-xl bg-bg-card" />
              <div className="rounded-b-xl bg-bg-card p-4">
                <div className="mb-2 h-6 w-3/4 rounded bg-bg-dark" />
                <div className="h-4 w-full rounded bg-bg-dark" />
                <div className="mt-2 h-4 w-2/3 rounded bg-bg-dark" />
              </div>
            </div>
          ))}
        </div>
      )}

      {error && (
        <EmptyState
          icon="connection"
          title="Failed to load shows"
          description={error.message || "We couldn't connect to the server. Please check your connection and try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      )}

      {shows && shows.length === 0 && (
        <EmptyState
          icon="tv"
          title="No shows yet"
          description="Generate your first lesson to start learning English from TV shows."
        >
          <Link
            to="/generate"
            className="mt-6 inline-flex items-center gap-2 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Generate a Lesson
          </Link>
        </EmptyState>
      )}

      {shows && shows.length > 0 && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {shows.map((show) => (
            <ShowCard key={show.id} show={show} />
          ))}
        </div>
      )}
    </div>
  );
}
