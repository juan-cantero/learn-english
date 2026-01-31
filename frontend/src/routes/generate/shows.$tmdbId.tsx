import { Link, useParams, useNavigate } from '@tanstack/react-router';
import { useShowSeasons } from '../../hooks/useGeneration';
import { ShowHero } from '../../components/generation/ShowHero';
import { SeasonGrid } from '../../components/generation/SeasonGrid';

export function ShowDetailPage() {
  const { tmdbId } = useParams({ from: '/generate/shows/$tmdbId' });
  const navigate = useNavigate();
  const { data, isLoading, error } = useShowSeasons(tmdbId);

  const handleSelectSeason = (seasonNumber: number) => {
    navigate({
      to: '/generate/shows/$tmdbId/seasons/$season',
      params: { tmdbId, season: String(seasonNumber) },
    });
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />
          <div className="flex gap-8">
            <div className="h-80 w-56 rounded-xl bg-bg-card" />
            <div className="flex-1 space-y-4">
              <div className="h-10 w-2/3 rounded bg-bg-card" />
              <div className="h-4 w-24 rounded bg-bg-card" />
              <div className="flex gap-2">
                <div className="h-8 w-24 rounded-full bg-bg-card" />
                <div className="h-8 w-28 rounded-full bg-bg-card" />
              </div>
              <div className="h-20 w-full rounded bg-bg-card" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to load show: {error.message}</p>
        </div>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Breadcrumb */}
      <Link
        to="/generate"
        className="mb-6 inline-flex items-center gap-2 text-sm text-text-secondary transition-colors hover:text-accent-primary"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to Search
      </Link>

      {/* Show Hero */}
      <ShowHero show={data.show} seasons={data.seasons} />

      {/* Seasons */}
      <div className="mt-10">
        <h2 className="mb-4 text-xl font-semibold text-text-primary">Select a Season</h2>
        <SeasonGrid seasons={data.seasons} onSelectSeason={handleSelectSeason} />
      </div>
    </div>
  );
}
