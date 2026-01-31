import type { EpisodeSummary } from '../../types/generation';

interface EpisodeListItemProps {
  episode: EpisodeSummary;
  onSelect: (episodeNumber: number) => void;
}

export function EpisodeListItem({ episode, onSelect }: EpisodeListItemProps) {
  return (
    <button
      onClick={() => onSelect(episode.episodeNumber)}
      className="group flex w-full items-start gap-4 rounded-xl border border-border bg-bg-card p-4 text-left transition-all hover:border-accent-primary/50 hover:bg-bg-card-hover focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark"
    >
      {/* Episode number badge */}
      <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-accent-primary/10 font-mono text-sm font-bold text-accent-primary transition-colors group-hover:bg-accent-primary group-hover:text-white">
        {episode.episodeNumber}
      </div>

      {/* Episode info */}
      <div className="min-w-0 flex-1">
        <h4 className="mb-1 font-semibold text-text-primary group-hover:text-accent-primary">
          {episode.title}
        </h4>
        {episode.overview && (
          <p className="line-clamp-2 text-sm text-text-secondary">
            {episode.overview}
          </p>
        )}
      </div>

      {/* Runtime and chevron */}
      <div className="flex flex-shrink-0 items-center gap-3">
        {episode.runtime && (
          <span className="font-mono text-sm text-text-secondary">
            {episode.runtime}m
          </span>
        )}
        <svg
          className="h-5 w-5 text-text-secondary transition-transform group-hover:translate-x-1 group-hover:text-accent-primary"
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
      </div>
    </button>
  );
}
