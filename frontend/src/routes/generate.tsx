import { useState } from 'react';
import { ShowSearch } from '../components/generation/ShowSearch';
import { EpisodeSelector } from '../components/generation/EpisodeSelector';
import { GenerateButton } from '../components/generation/GenerateButton';
import { useStartGeneration } from '../hooks/useGeneration';
import type { TMDBShow } from '../types/generation';
import type { Genre } from '../types/show';

export function GeneratePage() {
  const [selectedShow, setSelectedShow] = useState<TMDBShow | null>(null);
  const [selectedSeason, setSelectedSeason] = useState<number>(1);
  const [selectedEpisode, setSelectedEpisode] = useState<number>(1);
  const [selectedGenre, setSelectedGenre] = useState<Genre>('DRAMA');
  const [generationJobId, setGenerationJobId] = useState<string | null>(null);

  const { mutate: startGeneration, isPending, error } = useStartGeneration();

  const handleSelectShow = (show: TMDBShow) => {
    setSelectedShow(show);
  };

  const handleEpisodeSelect = (season: number, episode: number, genre: Genre) => {
    setSelectedSeason(season);
    setSelectedEpisode(episode);
    setSelectedGenre(genre);
  };

  const handleGenerate = () => {
    if (!selectedShow) return;

    startGeneration(
      {
        tmdbId: selectedShow.tmdbId,
        season: selectedSeason,
        episode: selectedEpisode,
        genre: selectedGenre,
      },
      {
        onSuccess: (job) => {
          setGenerationJobId(job.jobId);
        },
      }
    );
  };

  const isGenerateDisabled = !selectedShow;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-text-primary">
          Generate New Lesson
        </h1>
        <p className="mt-2 text-text-secondary">
          Search for a TV show, select an episode, and generate a new English learning lesson.
        </p>
      </div>

      <div className="space-y-8">
        {/* Step 1: Show Search */}
        <section className="rounded-xl border border-border bg-bg-card p-6">
          <div className="mb-4">
            <h2 className="text-xl font-semibold text-text-primary">
              Step 1: Select a Show
            </h2>
            <p className="mt-1 text-sm text-text-secondary">
              Search The Movie Database for the show you want to create a lesson from
            </p>
          </div>
          <ShowSearch onSelectShow={handleSelectShow} selectedShow={selectedShow} />
        </section>

        {/* Step 2: Episode Selection */}
        {selectedShow && (
          <section className="rounded-xl border border-border bg-bg-card p-6">
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-text-primary">
                Step 2: Choose Episode and Genre
              </h2>
              <p className="mt-1 text-sm text-text-secondary">
                Specify which episode and the show's genre
              </p>
            </div>
            <EpisodeSelector
              onSelect={handleEpisodeSelect}
              selectedSeason={selectedSeason}
              selectedEpisode={selectedEpisode}
              selectedGenre={selectedGenre}
            />
          </section>
        )}

        {/* Step 3: Generate */}
        {selectedShow && (
          <section className="rounded-xl border border-border bg-bg-card p-6">
            <div className="mb-4">
              <h2 className="text-xl font-semibold text-text-primary">
                Step 3: Generate Lesson
              </h2>
              <p className="mt-1 text-sm text-text-secondary">
                Start the AI-powered lesson generation process
              </p>
            </div>

            {error && (
              <div className="mb-4 rounded-lg border border-error/50 bg-error/10 p-4">
                <p className="text-error">Failed to start generation: {error.message}</p>
              </div>
            )}

            {generationJobId && (
              <div className="mb-4 rounded-lg border border-success/50 bg-success/10 p-4">
                <p className="text-success">
                  Generation started! Job ID: <span className="font-mono">{generationJobId}</span>
                </p>
                <p className="mt-2 text-sm text-text-secondary">
                  Track the progress in the next task (6.2 - Generation Progress Tracking)
                </p>
              </div>
            )}

            <GenerateButton
              onClick={handleGenerate}
              disabled={isGenerateDisabled}
              isLoading={isPending}
            />

            {isGenerateDisabled && (
              <p className="mt-2 text-center text-sm text-text-secondary">
                Please select a show and episode to continue
              </p>
            )}
          </section>
        )}
      </div>
    </div>
  );
}
