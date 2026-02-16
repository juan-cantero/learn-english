import { useParams } from '@tanstack/react-router';
import { useShow } from '../../hooks/useShows';
import { Navigation } from '../../components/layout/Navigation';
import { EpisodeList } from '../../components/show/EpisodeList';

const difficultyColors = {
  BEGINNER: 'bg-diff-beginner-bg text-diff-beginner',
  INTERMEDIATE: 'bg-diff-intermediate-bg text-diff-intermediate',
  ADVANCED: 'bg-diff-advanced-bg text-diff-advanced',
};

export function ShowPage() {
  const { slug } = useParams({ from: '/shows/$slug' });
  const { data, isLoading, error } = useShow(slug);

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-4 h-4 w-24 rounded bg-bg-card" />
          <div className="mb-8 flex gap-8">
            <div className="h-64 w-48 rounded-xl bg-bg-card" />
            <div className="flex-1">
              <div className="mb-4 h-8 w-2/3 rounded bg-bg-card" />
              <div className="h-4 w-full rounded bg-bg-card" />
              <div className="mt-2 h-4 w-3/4 rounded bg-bg-card" />
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

  const { show, episodes } = data;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <Navigation showTitle={show.title} showSlug={show.slug} />

      <div className="mt-8 flex flex-col gap-6 md:flex-row md:gap-8">
        <div className="shrink-0">
          <div className="mx-auto aspect-[3/4] w-full max-w-[200px] overflow-hidden rounded-xl bg-bg-card md:h-72 md:w-48">
            {show.imageUrl ? (
              <img
                src={show.imageUrl}
                alt={show.title}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center">
                <span className="text-5xl font-bold text-content-secondary">
                  {show.title.charAt(0)}
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1">
          <h1 className="text-2xl font-bold text-content-primary md:text-3xl">{show.title}</h1>

          <div className="mt-3 flex flex-wrap items-center gap-2">
            <span className="rounded-md bg-bg-card px-2.5 py-1 text-sm text-content-secondary">
              {show.genre}
            </span>
            <span className="rounded-md bg-bg-card px-2.5 py-1 text-sm text-content-secondary">
              {show.accent} English
            </span>
            <span
              className={`rounded-md px-2.5 py-1 text-sm ${difficultyColors[show.difficulty]}`}
            >
              {show.difficulty}
            </span>
          </div>

          <p className="mt-3 text-sm text-content-secondary md:mt-4 md:text-base">{show.description}</p>

          <div className="mt-4 flex gap-6 border-t border-edge-default pt-4 md:mt-6 md:pt-6">
            <div>
              <p className="font-mono text-2xl font-bold text-content-primary">
                {data.seasonCount}
              </p>
              <p className="text-sm text-content-secondary">
                {data.seasonCount === 1 ? 'Season' : 'Seasons'}
              </p>
            </div>
            <div>
              <p className="font-mono text-2xl font-bold text-content-primary">
                {data.episodeCount}
              </p>
              <p className="text-sm text-content-secondary">
                {data.episodeCount === 1 ? 'Episode' : 'Episodes'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-12">
        <h2 className="mb-6 text-2xl font-bold text-content-primary">Episodes</h2>
        <EpisodeList episodes={episodes} showSlug={show.slug} tmdbId={show.tmdbId} />
      </div>
    </div>
  );
}
