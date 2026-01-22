import { Link } from '@tanstack/react-router';
import type { EpisodeSummary } from '../../types/show';

interface EpisodeListProps {
  episodes: EpisodeSummary[];
  showSlug: string;
}

export function EpisodeList({ episodes, showSlug }: EpisodeListProps) {
  const groupedBySeason = episodes.reduce(
    (acc, episode) => {
      const season = episode.seasonNumber;
      if (!acc[season]) {
        acc[season] = [];
      }
      acc[season].push(episode);
      return acc;
    },
    {} as Record<number, EpisodeSummary[]>
  );

  const seasons = Object.keys(groupedBySeason)
    .map(Number)
    .sort((a, b) => a - b);

  return (
    <div className="space-y-8">
      {seasons.map((season) => (
        <div key={season}>
          <h3 className="mb-4 text-lg font-semibold text-text-primary">
            Season {season}
          </h3>
          <div className="space-y-2">
            {groupedBySeason[season]
              .sort((a, b) => a.episodeNumber - b.episodeNumber)
              .map((episode) => (
                <Link
                  key={episode.id}
                  to="/shows/$slug/episodes/$episodeSlug"
                  params={{ slug: showSlug, episodeSlug: episode.slug }}
                  className="group flex items-center gap-4 rounded-lg border border-border bg-bg-card p-4 transition-all hover:border-accent-primary/50 hover:bg-bg-card-hover"
                >
                  <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-bg-dark font-mono text-sm font-medium text-text-secondary group-hover:bg-accent-primary group-hover:text-white">
                    {episode.episodeNumber}
                  </span>
                  <div className="min-w-0 flex-1">
                    <h4 className="truncate font-medium text-text-primary group-hover:text-accent-primary">
                      {episode.title}
                    </h4>
                    <p className="mt-1 text-sm text-text-secondary">
                      S{episode.seasonNumber.toString().padStart(2, '0')}E
                      {episode.episodeNumber.toString().padStart(2, '0')} |{' '}
                      {episode.durationMinutes} min
                    </p>
                  </div>
                  <svg
                    className="h-5 w-5 shrink-0 text-text-secondary transition-transform group-hover:translate-x-1 group-hover:text-accent-primary"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </Link>
              ))}
          </div>
        </div>
      ))}
    </div>
  );
}
