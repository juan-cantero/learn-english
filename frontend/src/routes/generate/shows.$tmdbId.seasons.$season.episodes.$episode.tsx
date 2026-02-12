import { useState } from 'react';
import { Link, useParams } from '@tanstack/react-router';
import { useSeasonEpisodes, useStartGeneration } from '../../hooks/useGeneration';
import { useGenerationContext } from '../../context/GenerationContext';
import { GenreSelector } from '../../components/generation/GenreSelector';
import { GenerateButton } from '../../components/generation/GenerateButton';
import { EmptyState } from '../../components/shared/EmptyState';
import type { Genre } from '../../types/show';

function generateSlug(title: string): string {
  return title
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

export function EpisodeConfirmationPage() {
  const { tmdbId, season, episode } = useParams({
    from: '/generate/shows/$tmdbId/seasons/$season/episodes/$episode',
  });
  const seasonNumber = parseInt(season, 10);
  const episodeNumber = parseInt(episode, 10);

  const { data, isLoading, error: loadError, refetch } = useSeasonEpisodes(tmdbId, seasonNumber);
  const { mutate: startGenerationMutation, isPending, error: genError } = useStartGeneration();
  const { startGeneration, hasActiveJob } = useGenerationContext();

  const [selectedGenre, setSelectedGenre] = useState<Genre>('DRAMA');
  const [generationStarted, setGenerationStarted] = useState(false);

  const selectedEpisode = data?.episodes.find((ep) => ep.episodeNumber === episodeNumber);

  const handleGenerate = () => {
    if (!data) return;

    startGenerationMutation(
      {
        tmdbId,
        season: seasonNumber,
        episode: episodeNumber,
        genre: selectedGenre,
      },
      {
        onSuccess: (job) => {
          // Start tracking in global context
          startGeneration({
            jobId: job.jobId,
            tmdbId,
            showTitle: data.showTitle,
            showSlug: generateSlug(data.showTitle),
            season: seasonNumber,
            episode: episodeNumber,
          });
          setGenerationStarted(true);
        },
      }
    );
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          {/* Breadcrumb skeleton */}
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />

          {/* Episode card skeleton */}
          <div className="mb-8 rounded-xl border border-edge-default bg-bg-card p-6">
            <div className="mb-4 flex items-start gap-4">
              <div className="h-12 w-12 flex-shrink-0 rounded-full bg-bg-primary" />
              <div className="flex-1 space-y-2">
                <div className="h-7 w-3/4 rounded bg-bg-primary" />
                <div className="h-4 w-1/2 rounded bg-bg-primary" />
              </div>
            </div>
            <div className="space-y-2">
              <div className="h-4 w-full rounded bg-bg-primary" />
              <div className="h-4 w-5/6 rounded bg-bg-primary" />
            </div>
          </div>

          {/* Genre selector skeleton */}
          <div className="mb-8">
            <div className="mb-3 h-5 w-28 rounded bg-bg-card" />
            <div className="mb-4 h-4 w-64 rounded bg-bg-card" />
            <div className="flex flex-wrap gap-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-10 w-24 rounded-lg bg-bg-card" />
              ))}
            </div>
          </div>

          {/* Button skeleton */}
          <div className="flex justify-center">
            <div className="h-12 w-48 rounded-lg bg-bg-card" />
          </div>
        </div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to="/generate/shows/$tmdbId/seasons/$season"
          params={{ tmdbId, season }}
          className="mb-6 inline-flex items-center gap-2 text-sm text-content-secondary transition-colors hover:text-brand"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Season {seasonNumber}
        </Link>
        <EmptyState
          icon="connection"
          title="Failed to load episode"
          description={loadError.message || "We couldn't connect to the server. Please check your connection and try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      </div>
    );
  }

  if (!data || !selectedEpisode) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Link
          to="/generate/shows/$tmdbId/seasons/$season"
          params={{ tmdbId, season }}
          className="mb-6 inline-flex items-center gap-2 text-sm text-content-secondary transition-colors hover:text-brand"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back to Season {seasonNumber}
        </Link>
        <EmptyState
          icon="empty"
          title="Episode not found"
          description={`Episode ${episodeNumber} of Season ${seasonNumber} doesn't exist or couldn't be loaded.`}
        />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Breadcrumb */}
      <Link
        to="/generate/shows/$tmdbId/seasons/$season"
        params={{ tmdbId, season }}
        className="mb-6 inline-flex items-center gap-2 text-sm text-content-secondary transition-colors hover:text-brand"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to Season {seasonNumber}
      </Link>

      {/* Success Message */}
      {generationStarted && (
        <div className="mb-6 rounded-lg border border-success/50 bg-success/10 p-4">
          <div className="flex items-center gap-3">
            <svg className="h-5 w-5 text-success" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <div>
              <p className="font-medium text-success">Generation started!</p>
              <p className="text-sm text-content-secondary">
                You can continue browsing. Check the indicator in the header for progress.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Episode Summary */}
      <div className="mb-8 rounded-xl border border-edge-default bg-bg-card p-6">
        <div className="mb-4 flex items-start gap-4">
          <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-brand/10 font-mono text-lg font-bold text-brand">
            {episodeNumber}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-content-primary">{selectedEpisode.title}</h1>
            <p className="mt-1 text-sm text-content-secondary">
              {data.showTitle} - Season {seasonNumber}, Episode {episodeNumber}
              {selectedEpisode.runtime && ` · ${selectedEpisode.runtime} min`}
            </p>
          </div>
        </div>

        {selectedEpisode.overview && (
          <p className="text-content-secondary">{selectedEpisode.overview}</p>
        )}
      </div>

      {/* Genre Selection */}
      <div className="mb-8">
        <h2 className="mb-3 text-lg font-semibold text-content-primary">Select Genre</h2>
        <p className="mb-4 text-sm text-content-secondary">
          Choose the genre that best describes this show for better lesson context
        </p>
        <GenreSelector value={selectedGenre} onChange={setSelectedGenre} />
      </div>

      {/* Error */}
      {genError && (
        <div className="mb-6 rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to start generation: {genError.message}</p>
        </div>
      )}

      {/* Generate Button */}
      <div className="flex justify-center">
        <GenerateButton
          onClick={handleGenerate}
          disabled={generationStarted || hasActiveJob}
          isLoading={isPending}
        />
      </div>

      {/* Already generating hint */}
      {hasActiveJob && !generationStarted && (
        <p className="mt-4 text-center text-sm text-content-secondary">
          A lesson is already being generated. Check the header for progress.
        </p>
      )}
    </div>
  );
}
