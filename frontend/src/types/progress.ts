export interface UserProgress {
  id: string;
  episodeId: string;
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
