import { useQuery } from '@tanstack/react-query';
import { getShows, getShowBySlug } from '../api/shows';
import { useAuth } from '../context/AuthContext';
import type { Show, ShowWithEpisodes } from '../types/show';

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
