import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  searchShows,
  getShowSeasons,
  getSeasonEpisodes,
  startGeneration,
  getGenerationStatus,
} from '../api/generation';
import type {
  TMDBShow,
  GenerationJob,
  GenerationRequest,
  ShowWithSeasons,
  SeasonWithEpisodes,
} from '../types/generation';

export function useSearchShows(query: string) {
  return useQuery<TMDBShow[], Error>({
    queryKey: ['shows', 'search', query],
    queryFn: () => searchShows(query),
    enabled: query.length >= 2,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}

export function useShowSeasons(tmdbId: string | null) {
  return useQuery<ShowWithSeasons, Error>({
    queryKey: ['shows', tmdbId, 'seasons'],
    queryFn: () => getShowSeasons(tmdbId!),
    enabled: !!tmdbId,
    staleTime: 1000 * 60 * 30, // 30 minutes - show data doesn't change often
  });
}

export function useSeasonEpisodes(tmdbId: string | null, season: number | null) {
  return useQuery<SeasonWithEpisodes, Error>({
    queryKey: ['shows', tmdbId, 'seasons', season, 'episodes'],
    queryFn: () => getSeasonEpisodes(tmdbId!, season!),
    enabled: !!tmdbId && season !== null && season > 0,
    staleTime: 1000 * 60 * 30, // 30 minutes
  });
}

export function useStartGeneration() {
  const queryClient = useQueryClient();

  return useMutation<GenerationJob, Error, GenerationRequest>({
    mutationFn: startGeneration,
    onSuccess: () => {
      // Invalidate shows query to refresh the list after generation
      queryClient.invalidateQueries({ queryKey: ['shows'] });
    },
  });
}

export function useGenerationStatus(jobId: string | null) {
  return useQuery<GenerationJob, Error>({
    queryKey: ['generation', 'jobs', jobId],
    queryFn: () => getGenerationStatus(jobId!),
    enabled: !!jobId,
    refetchInterval: (query) => {
      const data = query.state.data;
      // Poll every 2 seconds if job is pending or processing
      if (data?.status === 'PENDING' || data?.status === 'PROCESSING') {
        return 2000;
      }
      // Stop polling if completed or failed
      return false;
    },
  });
}
