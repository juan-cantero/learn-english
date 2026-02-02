import { Link, useParams, useNavigate } from '@tanstack/react-router';
import { useSeasonEpisodes } from '../../hooks/useGeneration';
import { EpisodeListItem } from '../../components/generation/EpisodeListItem';
import { EmptyState } from '../../components/shared/EmptyState';

export function EpisodeListPage() {
  const { tmdbId, season } = useParams({
    from: '/generate/shows/$tmdbId/seasons/$season',
  });
  const navigate = useNavigate();
  const seasonNumber = parseInt(season, 10);
  const { data, isLoading, error, refetch } = useSeasonEpisodes(tmdbId, seasonNumber);

  const handleSelectEpisode = (episodeNumber: number) => {
    navigate({
      to: '/generate/shows/$tmdbId/seasons/$season/episodes/$episode',
      params: { tmdbId, season, episode: String(episodeNumber) },
    });
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          {/* Breadcrumb skeleton */}
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />

          {/* Header skeleton */}
          <div className="mb-2 h-7 w-48 rounded bg-bg-card sm:h-8 sm:w-64" />
          <div className="mb-6 h-4 w-24 rounded bg-bg-card" />

          {/* Episode list skeleton */}
          <div className="space-y-3">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="flex gap-4 rounded-xl bg-bg-card p-4">
                <div className="h-10 w-10 flex-shrink-0 rounded-full bg-bg-dark" />
                <div className="flex-1 space-y-2">
                  <div className="h-5 w-3/4 rounded bg-bg-dark" />
                  <div className="h-4 w-1/2 rounded bg-bg-dark" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to="/generate/shows/$tmdbId"
          params={{ tmdbId }}
          className="mb-6 inline-flex items-center gap-2 text-sm text-text-secondary transition-colors hover:text-accent-primary"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Show
        </Link>
        <EmptyState
          icon="connection"
          title="Failed to load episodes"
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
        to="/generate/shows/$tmdbId"
        params={{ tmdbId }}
        className="mb-6 inline-flex items-center gap-2 text-sm text-text-secondary transition-colors hover:text-accent-primary"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to {data.showTitle}
      </Link>

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-text-primary">
          {data.showTitle} - Season {seasonNumber}
        </h1>
        <p className="mt-1 text-text-secondary">
          {data.episodes.length} {data.episodes.length === 1 ? 'episode' : 'episodes'}
        </p>
      </div>

      {/* Episode List */}
      <div className="space-y-3">
        {data.episodes.map((episode) => (
          <EpisodeListItem
            key={episode.episodeNumber}
            episode={episode}
            onSelect={handleSelectEpisode}
          />
        ))}
      </div>
    </div>
  );
}
