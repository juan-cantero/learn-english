import { Link } from '@tanstack/react-router';
import type { Show } from '../../types/show';

interface ShowCardProps {
  show: Show;
}

const difficultyColors = {
  BEGINNER: 'bg-green-900/50 text-green-400',
  INTERMEDIATE: 'bg-yellow-900/50 text-yellow-400',
  ADVANCED: 'bg-red-900/50 text-red-400',
};

export function ShowCard({ show }: ShowCardProps) {
  return (
    <Link
      to="/shows/$slug"
      params={{ slug: show.slug }}
      className="group block overflow-hidden rounded-xl bg-bg-card transition-all hover:bg-bg-card-hover hover:ring-1 hover:ring-accent-primary/50 hover:scale-[1.02] hover:shadow-xl hover:shadow-accent-primary/10 focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark"
    >
      <div className="aspect-video overflow-hidden bg-bg-dark">
        {show.imageUrl ? (
          <img
            src={show.imageUrl}
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

      <div className="p-4">
        <h3 className="mb-2 text-lg font-semibold text-text-primary group-hover:text-accent-primary">
          {show.title}
        </h3>

        <p className="mb-3 line-clamp-2 text-sm text-text-secondary">
          {show.description}
        </p>

        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-md bg-bg-dark px-2 py-1 font-mono text-xs text-text-secondary">
            {show.totalSeasons} {show.totalSeasons === 1 ? 'Season' : 'Seasons'}
          </span>
          <span className="rounded-md bg-bg-dark px-2 py-1 font-mono text-xs text-text-secondary">
            {show.totalEpisodes} Episodes
          </span>
          <span
            className={`rounded-md px-2 py-1 font-mono text-xs ${difficultyColors[show.difficulty]}`}
          >
            {show.difficulty}
          </span>
        </div>

        <div className="mt-3 flex items-center gap-2 text-xs text-text-secondary">
          <span>{show.genre}</span>
          <span>|</span>
          <span>{show.accent} English</span>
        </div>
      </div>
    </Link>
  );
}
