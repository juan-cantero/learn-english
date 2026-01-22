import { useQuery } from '@tanstack/react-query';
import { getShows, getShowBySlug } from '../api/shows';
import type { Show, ShowWithEpisodes } from '../types/show';

export function useShows() {
  return useQuery<Show[], Error>({
    queryKey: ['shows'],
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
