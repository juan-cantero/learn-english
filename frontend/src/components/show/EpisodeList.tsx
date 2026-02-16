import { Link } from '@tanstack/react-router';
import { useSeasonEpisodes } from '../../hooks/useShows';
import type { EpisodeSummary, SeasonEpisodeInfo } from '../../types/show';

interface EpisodeListProps {
  episodes: EpisodeSummary[];
  showSlug: string;
  tmdbId: string | null;
}

interface SeasonEpisodeListProps {
  season: number;
  showSlug: string;
  tmdbId: string | null;
  generatedEpisodes: EpisodeSummary[];
}

function SeasonEpisodeList({ season, showSlug, tmdbId, generatedEpisodes }: SeasonEpisodeListProps) {
  const { data: tmdbEpisodes, isLoading, error } = useSeasonEpisodes(showSlug, season);

  // If TMDB data is available, use it; otherwise fall back to generated episodes only
  const episodesToShow: (SeasonEpisodeInfo | EpisodeSummary)[] =
    tmdbEpisodes && !isLoading && !error
      ? tmdbEpisodes
      : generatedEpisodes;

  const sortedEpisodes = [...episodesToShow].sort((a, b) => a.episodeNumber - b.episodeNumber);

  return (
    <div className="space-y-2">
      {sortedEpisodes.map((episode) => {
        // Check if this is a TMDB episode (has 'generated' property) or generated episode (has 'id' property)
        const isTmdbEpisode = 'generated' in episode;
        const isGenerated = isTmdbEpisode ? episode.generated : true;

        if (isGenerated) {
          // Generated episode - keep existing style
          const slug = isTmdbEpisode
            ? (episode as SeasonEpisodeInfo).slug!
            : (episode as EpisodeSummary).slug;
          const runtime = isTmdbEpisode
            ? (episode as SeasonEpisodeInfo).runtime
            : (episode as EpisodeSummary).durationMinutes;

          return (
            <Link
              key={`gen-${episode.episodeNumber}`}
              to="/shows/$slug/episodes/$episodeSlug"
              params={{ slug: showSlug, episodeSlug: slug }}
              className="group flex items-center gap-4 rounded-lg border border-edge-default bg-bg-card p-4 transition-all hover:border-brand/50 hover:bg-bg-card-hover active:scale-[0.98]"
            >
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-bg-primary font-mono text-sm font-medium text-content-secondary group-hover:bg-brand group-hover:text-white">
                {episode.episodeNumber}
              </span>
              <div className="min-w-0 flex-1">
                <h4 className="truncate font-medium text-content-primary group-hover:text-brand">
                  {episode.title}
                </h4>
                <p className="mt-1 text-sm text-content-secondary">
                  S{season.toString().padStart(2, '0')}E
                  {episode.episodeNumber.toString().padStart(2, '0')} |{' '}
                  {runtime} min
                </p>
              </div>
              <svg
                className="h-5 w-5 shrink-0 text-content-secondary transition-transform group-hover:translate-x-1 group-hover:text-brand"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </Link>
          );
        } else {
          // Available episode (not yet generated) - dashed border, muted, download icon
          const availableEp = episode as SeasonEpisodeInfo;

          // Only show generate link if tmdbId is available
          if (!tmdbId) return null;

          return (
            <Link
              key={availableEp.episodeNumber}
              to="/generate/shows/$tmdbId/seasons/$season/episodes/$episode"
              params={{
                tmdbId,
                season: season.toString(),
                episode: availableEp.episodeNumber.toString()
              }}
              className="group flex items-center gap-4 rounded-lg border border-dashed border-edge-default bg-bg-card p-4 opacity-70 transition-all hover:opacity-100 hover:border-brand/50 active:scale-[0.98]"
            >
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-brand-muted font-mono text-sm font-medium text-brand">
                {availableEp.episodeNumber}
              </span>
              <div className="min-w-0 flex-1">
                <h4 className="truncate font-medium text-content-primary">
                  {availableEp.title}
                </h4>
                <p className="mt-1 text-sm text-content-tertiary">
                  Generate lesson content
                </p>
              </div>
              <svg
                className="h-5 w-5 shrink-0 text-content-secondary transition-transform group-hover:translate-y-1"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                />
              </svg>
            </Link>
          );
        }
      })}
    </div>
  );
}

export function EpisodeList({ episodes, showSlug, tmdbId }: EpisodeListProps) {
  const groupedBySeason = episodes.reduce(
    (acc, episode) => {
      const season = episode.seasonNumber;
      if (!acc[season]) {
        acc[season] = [];
      }
      acc[season].push(episode);
      return acc;
    },
    {} as Record<number, EpisodeSummary[]>
  );

  const seasons = Object.keys(groupedBySeason)
    .map(Number)
    .sort((a, b) => a - b);

  return (
    <div className="space-y-8">
      {seasons.map((season) => (
        <div key={season}>
          <h3 className="mb-4 text-lg font-semibold text-content-primary">
            Season {season}
          </h3>
          <SeasonEpisodeList
            season={season}
            showSlug={showSlug}
            tmdbId={tmdbId}
            generatedEpisodes={groupedBySeason[season]}
          />
        </div>
      ))}
    </div>
  );
}
