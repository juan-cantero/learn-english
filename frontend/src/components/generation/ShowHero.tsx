import type { TMDBShow, SeasonSummary } from '../../types/generation';

interface ShowHeroProps {
  show: TMDBShow;
  seasons: SeasonSummary[];
}

export function ShowHero({ show, seasons }: ShowHeroProps) {
  const totalEpisodes = seasons.reduce((sum, s) => sum + s.episodeCount, 0);

  return (
    <div className="flex flex-col gap-6 sm:flex-row sm:gap-8">
      {/* Poster */}
      <div className="mx-auto w-48 flex-shrink-0 sm:mx-0 sm:w-56">
        <div className="aspect-[2/3] overflow-hidden rounded-xl bg-bg-inset shadow-xl">
          {show.posterUrl ? (
            <img
              src={show.posterUrl}
              alt={show.title}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center">
              <span className="text-6xl font-bold text-content-secondary">
                {show.title.charAt(0)}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Info */}
      <div className="flex-1 text-center sm:text-left">
        <h1 className="mb-2 text-3xl font-bold text-content-primary sm:text-4xl">
          {show.title}
        </h1>

        {show.year && (
          <p className="mb-4 font-mono text-sm text-content-secondary">
            {show.year}
          </p>
        )}

        {/* Badges */}
        <div className="mb-4 flex flex-wrap justify-center gap-2 sm:justify-start">
          <span className="inline-flex items-center rounded-full bg-brand-muted px-3 py-1 text-sm font-medium text-brand">
            {seasons.length} {seasons.length === 1 ? 'Season' : 'Seasons'}
          </span>
          <span className="inline-flex items-center rounded-full bg-bg-card px-3 py-1 text-sm font-medium text-content-secondary">
            {totalEpisodes} Episodes
          </span>
        </div>

        {show.overview && (
          <p className="text-content-secondary line-clamp-4 sm:line-clamp-none">
            {show.overview}
          </p>
        )}
      </div>
    </div>
  );
}
