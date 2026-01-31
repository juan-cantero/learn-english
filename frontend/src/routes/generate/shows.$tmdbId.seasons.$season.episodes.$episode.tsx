import { useState } from 'react';
import { Link, useParams, useNavigate } from '@tanstack/react-router';
import { useSeasonEpisodes, useStartGeneration } from '../../hooks/useGeneration';
import { GenreSelector } from '../../components/generation/GenreSelector';
import { GenerateButton } from '../../components/generation/GenerateButton';
import { GenerationProgress } from '../../components/generation/GenerationProgress';
import type { Genre } from '../../types/show';

export function EpisodeConfirmationPage() {
  const { tmdbId, season, episode } = useParams({
    from: '/generate/shows/$tmdbId/seasons/$season/episodes/$episode',
  });
  const navigate = useNavigate();
  const seasonNumber = parseInt(season, 10);
  const episodeNumber = parseInt(episode, 10);

  const { data, isLoading, error: loadError } = useSeasonEpisodes(tmdbId, seasonNumber);
  const { mutate: startGeneration, isPending, error: genError } = useStartGeneration();

  const [selectedGenre, setSelectedGenre] = useState<Genre>('DRAMA');
  const [generationJobId, setGenerationJobId] = useState<string | null>(null);

  const selectedEpisode = data?.episodes.find((ep) => ep.episodeNumber === episodeNumber);

  const handleGenerate = () => {
    startGeneration(
      {
        tmdbId,
        season: seasonNumber,
        episode: episodeNumber,
        genre: selectedGenre,
      },
      {
        onSuccess: (job) => {
          setGenerationJobId(job.jobId);
        },
      }
    );
  };

  const handleGenerationComplete = (episodeId: string) => {
    navigate({ to: `/shows/${episodeId}/episodes/${episodeId}` });
  };

  const handleGenerationError = () => {
    setGenerationJobId(null);
  };

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-6 h-4 w-32 rounded bg-bg-card" />
          <div className="h-8 w-64 rounded bg-bg-card" />
        </div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to load episode: {loadError.message}</p>
        </div>
      </div>
    );
  }

  if (!data || !selectedEpisode) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Episode not found</p>
        </div>
      </div>
    );
  }

  // Show progress if generation has started
  if (generationJobId) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <GenerationProgress
          jobId={generationJobId}
          onComplete={handleGenerationComplete}
          onError={handleGenerationError}
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
        className="mb-6 inline-flex items-center gap-2 text-sm text-text-secondary transition-colors hover:text-accent-primary"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Back to Season {seasonNumber}
      </Link>

      {/* Episode Summary */}
      <div className="mb-8 rounded-xl border border-border bg-bg-card p-6">
        <div className="mb-4 flex items-start gap-4">
          <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-accent-primary/10 font-mono text-lg font-bold text-accent-primary">
            {episodeNumber}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-text-primary">{selectedEpisode.title}</h1>
            <p className="mt-1 text-sm text-text-secondary">
              {data.showTitle} - Season {seasonNumber}, Episode {episodeNumber}
              {selectedEpisode.runtime && ` · ${selectedEpisode.runtime} min`}
            </p>
          </div>
        </div>

        {selectedEpisode.overview && (
          <p className="text-text-secondary">{selectedEpisode.overview}</p>
        )}
      </div>

      {/* Genre Selection */}
      <div className="mb-8">
        <h2 className="mb-3 text-lg font-semibold text-text-primary">Select Genre</h2>
        <p className="mb-4 text-sm text-text-secondary">
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
        <GenerateButton onClick={handleGenerate} disabled={false} isLoading={isPending} />
      </div>
    </div>
  );
}
