import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getProgressSnapshot, saveProgress } from '../api/progress';
import type { ProgressSnapshot, UserProgress, SaveProgressRequest } from '../types/progress';

export function useProgressSnapshot() {
  return useQuery<ProgressSnapshot, Error>({
    queryKey: ['progress'],
    queryFn: getProgressSnapshot,
  });
}

export function useSaveProgress(episodeId: string) {
  const queryClient = useQueryClient();

  return useMutation<
    UserProgress,
    Error,
    { category: SaveProgressRequest['category']; points: number }
  >({
    mutationFn: ({ category, points }) => saveProgress(episodeId, category, points),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['progress'] });
      queryClient.invalidateQueries({ queryKey: ['lesson'] });
    },
  });
}
