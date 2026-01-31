export interface TMDBShow {
  tmdbId: string;
  title: string;
  overview: string;
  posterPath: string | null;
  year: number;
}

export interface Season {
  seasonNumber: number;
  episodeCount: number;
}

export interface Episode {
  episodeNumber: number;
  title: string;
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
