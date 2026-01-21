import { apiGet, apiPost } from './client';
import type { ProgressSnapshot, UserProgress, SaveProgressRequest } from '../types/progress';

export async function getProgressSnapshot(): Promise<ProgressSnapshot> {
  return apiGet<ProgressSnapshot>('/progress');
}

export async function getEpisodeProgress(episodeId: string): Promise<UserProgress | null> {
  return apiGet<UserProgress | null>(`/progress/${episodeId}`);
}

export async function saveProgress(
  episodeId: string,
  category: SaveProgressRequest['category'],
  points: number
): Promise<UserProgress> {
  const request: SaveProgressRequest = { category, points };
  return apiPost<SaveProgressRequest, UserProgress>(`/progress/${episodeId}`, request);
}
