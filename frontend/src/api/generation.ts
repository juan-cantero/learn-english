import { apiGet, apiPost } from './client';
import type {
  TMDBShow,
  GenerationJob,
  GenerationRequest,
  ShowSearchResponse,
  ShowWithSeasons,
  SeasonWithEpisodes,
} from '../types/generation';

export async function searchShows(query: string): Promise<TMDBShow[]> {
  const response = await apiGet<ShowSearchResponse>(
    `/generation/shows/search?q=${encodeURIComponent(query)}`
  );
  return response.shows;
}

export async function getShowSeasons(tmdbId: string): Promise<ShowWithSeasons> {
  return apiGet<ShowWithSeasons>(`/generation/shows/${tmdbId}/seasons`);
}

export async function getSeasonEpisodes(
  tmdbId: string,
  season: number
): Promise<SeasonWithEpisodes> {
  return apiGet<SeasonWithEpisodes>(
    `/generation/shows/${tmdbId}/seasons/${season}/episodes`
  );
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
