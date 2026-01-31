import { Link } from '@tanstack/react-router';
import type { TMDBShow } from '../../types/generation';

interface SearchShowCardProps {
  show: TMDBShow;
}

export function SearchShowCard({ show }: SearchShowCardProps) {
  // Backend already returns full posterUrl from TMDB
  const posterUrl = show.posterUrl;

  return (
    <Link
      to="/generate/shows/$tmdbId"
      params={{ tmdbId: show.tmdbId }}
      className="group block w-full overflow-hidden rounded-xl bg-bg-card text-left transition-all hover:bg-bg-card-hover hover:scale-[1.02] hover:shadow-xl hover:shadow-accent-primary/10 focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark hover:ring-1 hover:ring-accent-primary/50"
    >
      <div className="aspect-[2/3] overflow-hidden bg-bg-dark">
        {posterUrl ? (
          <img
            src={posterUrl}
            alt={show.title}
            className="h-full w-full object-cover transition-transform group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <span className="text-4xl font-bold text-text-secondary">
              {show.title.charAt(0)}
            </span>
          </div>
        )}
      </div>

      <div className="p-3 sm:p-4">
        <h3 className="mb-1 text-sm font-semibold text-text-primary group-hover:text-accent-primary sm:text-lg">
          {show.title}
        </h3>

        <p className="font-mono text-xs text-text-secondary">
          {show.year}
        </p>

        <p className="mt-2 hidden line-clamp-2 text-sm text-text-secondary sm:block sm:line-clamp-3">
          {show.overview}
        </p>
      </div>
    </Link>
  );
}
