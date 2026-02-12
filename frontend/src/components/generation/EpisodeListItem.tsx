import type { EpisodeSummary } from '../../types/generation';

interface EpisodeListItemProps {
  episode: EpisodeSummary;
  onSelect: (episodeNumber: number) => void;
}

export function EpisodeListItem({ episode, onSelect }: EpisodeListItemProps) {
  return (
    <button
      onClick={() => onSelect(episode.episodeNumber)}
      className="group flex w-full items-center gap-3 rounded-xl border border-edge-default bg-bg-card p-4 text-left transition-all hover:border-brand/50 hover:bg-bg-card-hover focus:outline-none focus:ring-2 focus:ring-brand focus:ring-offset-2 focus:ring-offset-bg-primary active:scale-[0.98] sm:items-start sm:gap-4"
    >
      {/* Episode number badge - min 44px for touch */}
      <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-brand/10 font-mono text-sm font-bold text-brand transition-colors group-hover:bg-brand group-hover:text-white">
        {episode.episodeNumber}
      </div>

      {/* Episode info */}
      <div className="min-w-0 flex-1">
        <h4 className="text-sm font-semibold text-content-primary group-hover:text-brand sm:mb-1 sm:text-base">
          {episode.title}
        </h4>
        {episode.overview && (
          <p className="mt-1 hidden line-clamp-2 text-sm text-content-secondary sm:block">
            {episode.overview}
          </p>
        )}
      </div>

      {/* Runtime and chevron */}
      <div className="flex flex-shrink-0 items-center gap-2 sm:gap-3">
        {episode.runtime && (
          <span className="font-mono text-xs text-content-secondary sm:text-sm">
            {episode.runtime}m
          </span>
        )}
        <svg
          className="h-5 w-5 text-content-secondary transition-transform group-hover:translate-x-1 group-hover:text-brand"
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
