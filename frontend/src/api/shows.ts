import { apiGet } from './client';
import type { Show, ShowWithEpisodes, SeasonEpisodeInfo } from '../types/show';

export async function getShows(): Promise<Show[]> {
  return apiGet<Show[]>('/shows');
}

export async function getShowBySlug(slug: string): Promise<ShowWithEpisodes> {
  return apiGet<ShowWithEpisodes>(`/shows/${slug}`);
}

export async function getSeasonEpisodes(slug: string, season: number): Promise<SeasonEpisodeInfo[]> {
  return apiGet<SeasonEpisodeInfo[]>(`/shows/${slug}/seasons/${season}/episodes`);
}
