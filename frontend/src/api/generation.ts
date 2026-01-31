import { apiGet, apiPost } from './client';
import type { TMDBShow, GenerationJob, GenerationRequest } from '../types/generation';

export async function searchShows(query: string): Promise<TMDBShow[]> {
  return apiGet<TMDBShow[]>(`/generation/shows/search?q=${encodeURIComponent(query)}`);
}

export async function startGeneration(request: GenerationRequest): Promise<GenerationJob> {
  const { tmdbId, season, episode, genre } = request;
  return apiPost<void, GenerationJob>(
    `/generation/lessons?tmdbId=${tmdbId}&season=${season}&episode=${episode}&genre=${genre}`,
    undefined as void
  );
}

export async function getGenerationStatus(jobId: string): Promise<GenerationJob> {
  return apiGet<GenerationJob>(`/generation/jobs/${jobId}`);
}
