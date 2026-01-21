import { useParams } from '@tanstack/react-router';
import { useShow } from '../../hooks/useShows';
import { Navigation } from '../../components/layout/Navigation';
import { EpisodeList } from '../../components/show/EpisodeList';

const difficultyColors = {
  BEGINNER: 'bg-green-900/50 text-green-400',
  INTERMEDIATE: 'bg-yellow-900/50 text-yellow-400',
  ADVANCED: 'bg-red-900/50 text-red-400',
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

      <div className="mt-8 flex flex-col gap-8 md:flex-row">
        <div className="shrink-0">
          <div className="h-72 w-48 overflow-hidden rounded-xl bg-bg-card">
            {show.imageUrl ? (
              <img
                src={show.imageUrl}
                alt={show.title}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center">
                <span className="text-5xl font-bold text-text-secondary">
                  {show.title.charAt(0)}
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1">
          <h1 className="text-3xl font-bold text-text-primary">{show.title}</h1>

          <div className="mt-4 flex flex-wrap items-center gap-3">
            <span className="rounded-md bg-bg-card px-3 py-1 text-sm text-text-secondary">
              {show.genre}
            </span>
            <span className="rounded-md bg-bg-card px-3 py-1 text-sm text-text-secondary">
              {show.accent} English
            </span>
            <span
              className={`rounded-md px-3 py-1 text-sm ${difficultyColors[show.difficulty]}`}
            >
              {show.difficulty}
            </span>
          </div>

          <p className="mt-4 text-text-secondary">{show.description}</p>

          <div className="mt-6 flex gap-6 border-t border-border pt-6">
            <div>
              <p className="font-mono text-2xl font-bold text-text-primary">
                {data.seasonCount}
              </p>
              <p className="text-sm text-text-secondary">
                {data.seasonCount === 1 ? 'Season' : 'Seasons'}
              </p>
            </div>
            <div>
              <p className="font-mono text-2xl font-bold text-text-primary">
                {data.episodeCount}
              </p>
              <p className="text-sm text-text-secondary">
                {data.episodeCount === 1 ? 'Episode' : 'Episodes'}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-12">
        <h2 className="mb-6 text-2xl font-bold text-text-primary">Episodes</h2>
        <EpisodeList episodes={episodes} showSlug={show.slug} />
      </div>
    </div>
  );
}
