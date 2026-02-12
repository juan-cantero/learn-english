import type { SeasonSummary } from '../../types/generation';

interface SeasonCardProps {
  season: SeasonSummary;
  onSelect: (seasonNumber: number) => void;
}

export function SeasonCard({ season, onSelect }: SeasonCardProps) {
  return (
    <button
      onClick={() => onSelect(season.seasonNumber)}
      className="group flex w-full items-center justify-between rounded-xl border border-edge-default bg-bg-card p-4 text-left transition-all hover:border-brand/50 hover:bg-bg-card-hover hover:shadow-lg hover:shadow-brand/5 focus:outline-none focus:ring-2 focus:ring-brand focus:ring-offset-2 focus:ring-offset-bg-primary active:scale-[0.98]"
    >
      <div className="flex items-center gap-4">
        {/* Season number badge */}
        <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-lg bg-brand/10 font-mono text-xl font-bold text-brand transition-colors group-hover:bg-brand group-hover:text-white">
          {season.seasonNumber}
        </div>

        <div>
          <h3 className="font-semibold text-content-primary group-hover:text-brand">
            {season.name || `Season ${season.seasonNumber}`}
          </h3>
          <p className="text-sm text-content-secondary">
            {season.episodeCount} {season.episodeCount === 1 ? 'episode' : 'episodes'}
          </p>
        </div>
      </div>

      {/* Chevron */}
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
    </button>
  );
}
