import type { SeasonSummary } from '../../types/generation';

interface SeasonCardProps {
  season: SeasonSummary;
  onSelect: (seasonNumber: number) => void;
}

export function SeasonCard({ season, onSelect }: SeasonCardProps) {
  return (
    <button
      onClick={() => onSelect(season.seasonNumber)}
      className="group flex w-full items-center justify-between rounded-xl border border-border bg-bg-card p-4 text-left transition-all hover:border-accent-primary/50 hover:bg-bg-card-hover hover:shadow-lg hover:shadow-accent-primary/5 focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark active:scale-[0.98]"
    >
      <div className="flex items-center gap-4">
        {/* Season number badge */}
        <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-lg bg-accent-primary/10 font-mono text-xl font-bold text-accent-primary transition-colors group-hover:bg-accent-primary group-hover:text-white">
          {season.seasonNumber}
        </div>

        <div>
          <h3 className="font-semibold text-text-primary group-hover:text-accent-primary">
            {season.name || `Season ${season.seasonNumber}`}
          </h3>
          <p className="text-sm text-text-secondary">
            {season.episodeCount} {season.episodeCount === 1 ? 'episode' : 'episodes'}
          </p>
        </div>
      </div>

      {/* Chevron */}
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
    </button>
  );
}
