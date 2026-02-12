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
      className="group block w-full overflow-hidden rounded-xl bg-bg-card text-left transition-all hover:bg-bg-card-hover hover:scale-[1.02] hover:shadow-xl hover:shadow-brand/10 focus:outline-none focus:ring-2 focus:ring-brand focus:ring-offset-2 focus:ring-offset-bg-primary hover:ring-1 hover:ring-brand/50"
    >
      <div className="aspect-[2/3] overflow-hidden bg-bg-inset">
        {posterUrl ? (
          <img
            src={posterUrl}
            alt={show.title}
            className="h-full w-full object-cover transition-transform group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <span className="text-4xl font-bold text-content-secondary">
              {show.title.charAt(0)}
            </span>
          </div>
        )}
      </div>

      <div className="p-3 sm:p-4">
        <h3 className="mb-1 text-sm font-semibold text-content-primary group-hover:text-brand sm:text-lg">
          {show.title}
        </h3>

        <p className="font-mono text-xs text-content-secondary">
          {show.year}
        </p>

        <p className="mt-2 hidden line-clamp-2 text-sm text-content-secondary sm:block sm:line-clamp-3">
          {show.overview}
        </p>
      </div>
    </Link>
  );
}
