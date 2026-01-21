import { apiGet } from './client';
import type { Show, ShowWithEpisodes } from '../types/show';

export async function getShows(): Promise<Show[]> {
  return apiGet<Show[]>('/shows');
}

export async function getShowBySlug(slug: string): Promise<ShowWithEpisodes> {
  return apiGet<ShowWithEpisodes>(`/shows/${slug}`);
}
