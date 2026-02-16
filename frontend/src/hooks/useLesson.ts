import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getLesson, checkAnswer, getShadowingScenes } from '../api/episodes';
import type { Lesson, AnswerResult, ShadowingScene } from '../types/lesson';

export function useLesson(showSlug: string | undefined, episodeSlug: string | undefined) {
  return useQuery<Lesson, Error>({
    queryKey: ['lesson', showSlug, episodeSlug],
    queryFn: () => getLesson(showSlug!, episodeSlug!),
    enabled: !!showSlug && !!episodeSlug,
  });
}

export function useCheckAnswer(showSlug: string, episodeSlug: string) {
  const queryClient = useQueryClient();

  return useMutation<AnswerResult, Error, { exerciseId: string; answer: string }>({
    mutationFn: ({ exerciseId, answer }) =>
      checkAnswer(showSlug, episodeSlug, exerciseId, answer),
    onSuccess: (result) => {
      queryClient.setQueryData<Lesson>(
        ['lesson', showSlug, episodeSlug],
        (oldData) => {
          if (!oldData) return oldData;
          return {
            ...oldData,
            progress: {
              ...oldData.progress,
              earnedPoints: result.totalProgressPoints,
              completionPercentage: result.progressPercentage,
              isComplete: result.lessonComplete,
              exercisesScore:
                oldData.progress.exercisesScore + (result.correct ? result.pointsEarned : 0),
            },
          };
        }
      );
    },
  });
}

export function useShadowingScenes(showSlug: string, episodeSlug: string, enabled = true) {
  return useQuery<ShadowingScene[], Error>({
    queryKey: ['shadowing', showSlug, episodeSlug],
    queryFn: () => getShadowingScenes(showSlug, episodeSlug),
    enabled,
    staleTime: Infinity, // Scenes are cached forever on the backend
  });
}
