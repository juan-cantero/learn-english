import { useShows } from '../hooks/useShows';
import { ShowCard } from '../components/show/ShowCard';

export function HomePage() {
  const { data: shows, isLoading, error } = useShows();

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
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to load shows: {error.message}</p>
        </div>
      )}

      {shows && shows.length === 0 && (
        <div className="rounded-lg border border-border bg-bg-card p-8 text-center">
          <p className="text-text-secondary">No shows available yet.</p>
        </div>
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
