import { Link, useParams, useNavigate } from '@tanstack/react-router';
import { useShowSeasons } from '../../hooks/useGeneration';
import { ShowHero } from '../../components/generation/ShowHero';
import { SeasonGrid } from '../../components/generation/SeasonGrid';
import { EmptyState } from '../../components/shared/EmptyState';

export function ShowDetailPage() {
  const { tmdbId } = useParams({ from: '/generate/shows/$tmdbId' });
  const navigate = useNavigate();
  const { data, isLoading, error, refetch } = useShowSeasons(tmdbId);

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
          {/* Breadcrumb skeleton */}
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />

          {/* Hero skeleton - responsive layout */}
          <div className="flex flex-col gap-6 sm:flex-row sm:gap-8">
            {/* Poster skeleton */}
            <div className="mx-auto aspect-[2/3] w-40 flex-shrink-0 rounded-xl bg-bg-card sm:mx-0 sm:w-56" />

            {/* Info skeleton */}
            <div className="flex-1 space-y-4">
              <div className="h-8 w-3/4 rounded bg-bg-card sm:h-10" />
              <div className="h-4 w-24 rounded bg-bg-card" />
              <div className="flex flex-wrap gap-2">
                <div className="h-7 w-20 rounded-full bg-bg-card sm:h-8 sm:w-24" />
                <div className="h-7 w-24 rounded-full bg-bg-card sm:h-8 sm:w-28" />
              </div>
              <div className="space-y-2">
                <div className="h-4 w-full rounded bg-bg-card" />
                <div className="h-4 w-5/6 rounded bg-bg-card" />
                <div className="h-4 w-4/6 rounded bg-bg-card" />
              </div>
            </div>
          </div>

          {/* Seasons skeleton */}
          <div className="mt-10">
            <div className="mb-4 h-6 w-40 rounded bg-bg-card" />
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="h-24 rounded-xl bg-bg-card" />
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to="/generate"
          className="mb-6 inline-flex items-center gap-2 text-sm text-content-secondary transition-colors hover:text-brand"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Search
        </Link>
        <EmptyState
          icon="connection"
          title="Failed to load show"
          description={error.message || "We couldn't connect to the server. Please check your connection and try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Breadcrumb */}
      <Link
        to="/generate"
        className="mb-6 inline-flex items-center gap-2 text-sm text-content-secondary transition-colors hover:text-brand"
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
        <h2 className="mb-4 text-xl font-semibold text-content-primary">Select a Season</h2>
        <SeasonGrid seasons={data.seasons} onSelectSeason={handleSelectSeason} />
      </div>
    </div>
  );
}
