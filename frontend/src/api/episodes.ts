import { apiGet, apiPost } from './client';
import type { Lesson, AnswerResult, CheckAnswerRequest, ShadowingScene } from '../types/lesson';

export async function getLesson(
  showSlug: string,
  episodeSlug: string
): Promise<Lesson> {
  return apiGet<Lesson>(`/shows/${showSlug}/episodes/${episodeSlug}`);
}

export async function checkAnswer(
  showSlug: string,
  episodeSlug: string,
  exerciseId: string,
  answer: string
): Promise<AnswerResult> {
  const request: CheckAnswerRequest = { answer };
  return apiPost<CheckAnswerRequest, AnswerResult>(
    `/shows/${showSlug}/episodes/${episodeSlug}/exercises/${exerciseId}/check`,
    request
  );
}

export async function getShadowingScenes(
  showSlug: string,
  episodeSlug: string
): Promise<ShadowingScene[]> {
  return apiGet<ShadowingScene[]>(`/shows/${showSlug}/episodes/${episodeSlug}/shadowing`);
}
