export interface TMDBShow {
  tmdbId: string;
  title: string;
  overview: string;
  posterUrl: string | null;  // Backend returns full URL, not path
  year: number;
}

export interface ShowSearchResponse {
  shows: TMDBShow[];
  totalResults: number;
}

export interface SeasonSummary {
  seasonNumber: number;
  name: string;
  episodeCount: number;
}

export interface EpisodeSummary {
  episodeNumber: number;
  title: string;
  overview: string;
  runtime: number | null;
}

export interface ShowWithSeasons {
  show: TMDBShow;
  seasons: SeasonSummary[];
}

export interface SeasonWithEpisodes {
  tmdbId: string;
  showTitle: string;
  season: number;
  episodes: EpisodeSummary[];
}

export type GenerationStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface GenerationJob {
  jobId: string;
  status: GenerationStatus;
  progress: number;
  currentStep: string;
  episodeId: string | null;
  error?: string;
}

export interface GenerationRequest {
  tmdbId: string;
  season: number;
  episode: number;
  genre: string;
}
