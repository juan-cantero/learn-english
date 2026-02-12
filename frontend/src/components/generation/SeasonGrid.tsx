import type { SeasonSummary } from '../../types/generation';
import { SeasonCard } from './SeasonCard';

interface SeasonGridProps {
  seasons: SeasonSummary[];
  onSelectSeason: (seasonNumber: number) => void;
}

export function SeasonGrid({ seasons, onSelectSeason }: SeasonGridProps) {
  if (seasons.length === 0) {
    return (
      <div className="rounded-xl border border-edge-default bg-bg-card p-8 text-center">
        <p className="text-content-secondary">No seasons available</p>
      </div>
    );
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {seasons.map((season) => (
        <SeasonCard
          key={season.seasonNumber}
          season={season}
          onSelect={onSelectSeason}
        />
      ))}
    </div>
  );
}
