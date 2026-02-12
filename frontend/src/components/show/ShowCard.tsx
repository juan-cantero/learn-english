import { Link } from '@tanstack/react-router';
import type { Show } from '../../types/show';

interface ShowCardProps {
  show: Show;
}

const difficultyColors = {
  BEGINNER: 'bg-diff-beginner-bg text-diff-beginner',
  INTERMEDIATE: 'bg-diff-intermediate-bg text-diff-intermediate',
  ADVANCED: 'bg-diff-advanced-bg text-diff-advanced',
};

export function ShowCard({ show }: ShowCardProps) {
  return (
    <Link
      to="/shows/$slug"
      params={{ slug: show.slug }}
      className="group block overflow-hidden rounded-xl bg-bg-card shadow-sm transition-all hover:bg-bg-card-hover hover:ring-1 hover:ring-brand/50 hover:scale-[1.01] hover:shadow-lg hover:shadow-brand/10 focus:outline-none focus:ring-2 focus:ring-brand focus:ring-offset-2 focus:ring-offset-bg-primary"
    >
      <div className="aspect-video overflow-hidden bg-bg-inset">
        {show.imageUrl ? (
          <img
            src={show.imageUrl}
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

      <div className="p-4">
        <h3 className="mb-2 text-lg font-semibold text-content-primary group-hover:text-brand">
          {show.title}
        </h3>

        <p className="mb-3 line-clamp-2 text-sm text-content-secondary">
          {show.description}
        </p>

        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-md bg-bg-inset px-2 py-1 font-mono text-xs text-content-secondary">
            {show.totalSeasons} {show.totalSeasons === 1 ? 'Season' : 'Seasons'}
          </span>
          <span className="rounded-md bg-bg-inset px-2 py-1 font-mono text-xs text-content-secondary">
            {show.totalEpisodes} Episodes
          </span>
          <span
            className={`rounded-md px-2 py-1 font-mono text-xs ${difficultyColors[show.difficulty]}`}
          >
            {show.difficulty}
          </span>
        </div>

        <div className="mt-3 flex items-center gap-2 text-xs text-content-secondary">
          <span>{show.genre}</span>
          <span>|</span>
          <span>{show.accent} English</span>
        </div>
      </div>
    </Link>
  );
}
