export interface EpisodeMetadata {
  title: string;
  showSlug: string;
  episodeSlug: string;
  seasonNumber: number;
  episodeNumber: number;
}

export interface UserProgress {
  id: string;
  episodeId: string;
  episode: EpisodeMetadata | null;
  vocabularyScore: number;
  grammarScore: number;
  expressionsScore: number;
  exercisesScore: number;
  totalPoints: number;
  completed: boolean;
  lastAccessed: string;
}

export interface ProgressSnapshot {
  totalLessonsStarted: number;
  totalLessonsCompleted: number;
  totalPoints: number;
  recentProgress: UserProgress[];
}

export interface SaveProgressRequest {
  category: 'vocabulary' | 'grammar' | 'expressions' | 'exercises';
  points: number;
}
