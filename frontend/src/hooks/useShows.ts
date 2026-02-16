import { useQuery } from '@tanstack/react-query';
import { getShows, getShowBySlug, getSeasonEpisodes } from '../api/shows';
import { useAuth } from '../context/AuthContext';
import type { Show, ShowWithEpisodes, SeasonEpisodeInfo } from '../types/show';

export function useShows() {
  const { user } = useAuth();
  return useQuery<Show[], Error>({
    queryKey: ['shows', user?.id ?? 'anonymous'],
    queryFn: getShows,
  });
}

export function useShow(slug: string | undefined) {
  return useQuery<ShowWithEpisodes, Error>({
    queryKey: ['shows', slug],
    queryFn: () => getShowBySlug(slug!),
    enabled: !!slug,
  });
}

export function useSeasonEpisodes(slug: string, season: number) {
  return useQuery<SeasonEpisodeInfo[], Error>({
    queryKey: ['shows', slug, 'seasons', season, 'episodes'],
    queryFn: () => getSeasonEpisodes(slug, season),
  });
}
