export type Genre =
  | 'DRAMA'
  | 'COMEDY'
  | 'THRILLER'
  | 'MEDICAL'
  | 'CRIME'
  | 'DOCUMENTARY'
  | 'ANIMATION'
  | 'SCIENCE_FICTION'
  | 'FANTASY'
  | 'ACTION';

export type AccentType = 'AMERICAN' | 'BRITISH' | 'AUSTRALIAN' | 'MIXED';

export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export interface Show {
  id: string;
  title: string;
  slug: string;
  description: string;
  genre: Genre;
  accent: AccentType;
  difficulty: DifficultyLevel;
  imageUrl: string;
  totalSeasons: number;
  totalEpisodes: number;
  tmdbId: string | null;
}

export interface EpisodeSummary {
  id: string;
  seasonNumber: number;
  episodeNumber: number;
  title: string;
  slug: string;
  durationMinutes: number;
}

export interface SeasonEpisodeInfo {
  episodeNumber: number;
  title: string;
  runtime: number;
  generated: boolean;
  slug: string | null;
}

export interface ShowWithEpisodes {
  show: Show;
  episodes: EpisodeSummary[];
  seasonCount: number;
  episodeCount: number;
}
