import { Link, useParams, useNavigate } from '@tanstack/react-router';
import { useSeasonEpisodes } from '../../hooks/useGeneration';
import { EpisodeListItem } from '../../components/generation/EpisodeListItem';

export function EpisodeListPage() {
  const { tmdbId, season } = useParams({
    from: '/generate/shows/$tmdbId/seasons/$season',
  });
  const navigate = useNavigate();
  const seasonNumber = parseInt(season, 10);
  const { data, isLoading, error } = useSeasonEpisodes(tmdbId, seasonNumber);

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
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />
          <div className="mb-6 h-8 w-64 rounded bg-bg-card" />
          <div className="space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-20 rounded-xl bg-bg-card" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to load episodes: {error.message}</p>
        </div>
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
