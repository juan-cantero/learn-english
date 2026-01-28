export type VocabularyCategory =
  | 'MEDICAL'
  | 'TECHNICAL'
  | 'SLANG'
  | 'IDIOM'
  | 'PROFESSIONAL'
  | 'EVERYDAY'
  | 'EMOTIONAL'
  | 'COLLOQUIAL'
  | 'ACTION';

export type ExerciseType =
  | 'FILL_IN_BLANK'
  | 'MATCHING'
  | 'MULTIPLE_CHOICE'
  | 'LISTENING';

export interface Episode {
  id: string;
  showSlug: string;
  seasonNumber: number;
  episodeNumber: number;
  title: string;
  slug: string;
  synopsis: string;
  durationMinutes: number;
}

export interface Vocabulary {
  id: string;
  term: string;
  definition: string;
  phonetic: string | null;
  category: VocabularyCategory;
  exampleSentence: string | null;
  contextTimestamp: string | null;
}

export interface GrammarPoint {
  id: string;
  title: string;
  explanation: string;
  structure: string | null;
  example: string | null;
  contextQuote: string | null;
}

export interface Expression {
  id: string;
  phrase: string;
  meaning: string;
  contextQuote: string | null;
  usageNote: string | null;
}

export interface Exercise {
  id: string;
  type: ExerciseType;
  question: string;
  options: string | null;
  matchingPairs: string | null;
  points: number;
  hint: string | null;
}

export interface ProgressSummary {
  earnedPoints: number;
  totalPoints: number;
  completionPercentage: number;
  isComplete: boolean;
  vocabularyScore: number;
  grammarScore: number;
  expressionsScore: number;
  exercisesScore: number;
}

export interface Lesson {
  episode: Episode;
  vocabulary: Vocabulary[];
  grammarPoints: GrammarPoint[];
  expressions: Expression[];
  exercises: Exercise[];
  progress: ProgressSummary;
}

export interface AnswerResult {
  exerciseId: string;
  correct: boolean;
  pointsEarned: number;
  correctAnswer: string | null;
  totalProgressPoints: number;
  lessonTotalPoints: number;
  progressPercentage: number;
  lessonComplete: boolean;
}

export interface CheckAnswerRequest {
  answer: string;
}
