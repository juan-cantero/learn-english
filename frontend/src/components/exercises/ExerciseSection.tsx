import { useState } from 'react';
import type { Exercise } from '../../types/lesson';
import { FillInBlank } from './FillInBlank';
import { MultipleChoice } from './MultipleChoice';
import { MatchingGame } from './MatchingGame';
import { ListeningExercise } from './ListeningExercise';

interface ExerciseSectionProps {
  exercises: Exercise[];
  showSlug: string;
  episodeSlug: string;
}

type ExerciseTabId = 'all' | 'FILL_IN_BLANK' | 'MULTIPLE_CHOICE' | 'MATCHING' | 'LISTENING';

export function ExerciseSection({ exercises, showSlug, episodeSlug }: ExerciseSectionProps) {
  const [activeTab, setActiveTab] = useState<ExerciseTabId>('all');

  const tabs: { id: ExerciseTabId; label: string }[] = [
    { id: 'all', label: 'All' },
    { id: 'FILL_IN_BLANK', label: 'Fill in Blank' },
    { id: 'MULTIPLE_CHOICE', label: 'Multiple Choice' },
    { id: 'MATCHING', label: 'Matching' },
    { id: 'LISTENING', label: 'Listening' },
  ];

  const filteredExercises =
    activeTab === 'all' ? exercises : exercises.filter((e) => e.type === activeTab);

  const renderExercise = (exercise: Exercise) => {
    switch (exercise.type) {
      case 'FILL_IN_BLANK':
        return (
          <FillInBlank
            key={exercise.id}
            exercise={exercise}
            showSlug={showSlug}
            episodeSlug={episodeSlug}
          />
        );
      case 'MULTIPLE_CHOICE':
        return (
          <MultipleChoice
            key={exercise.id}
            exercise={exercise}
            showSlug={showSlug}
            episodeSlug={episodeSlug}
          />
        );
      case 'MATCHING':
        return (
          <MatchingGame
            key={exercise.id}
            exercise={exercise}
            showSlug={showSlug}
            episodeSlug={episodeSlug}
          />
        );
      case 'LISTENING':
        return (
          <ListeningExercise
            key={exercise.id}
            exercise={exercise}
            showSlug={showSlug}
            episodeSlug={episodeSlug}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <div className="mb-6 flex flex-wrap gap-2">
        {tabs.map((tab) => {
          const count =
            tab.id === 'all'
              ? exercises.length
              : exercises.filter((e) => e.type === tab.id).length;

          if (count === 0 && tab.id !== 'all') return null;

          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-all ${
                activeTab === tab.id
                  ? 'bg-accent-primary text-white'
                  : 'bg-bg-card text-text-secondary hover:bg-bg-card-hover hover:text-text-primary'
              }`}
            >
              {tab.label}
              <span className="ml-2 font-mono text-xs opacity-70">{count}</span>
            </button>
          );
        })}
      </div>

      <div className="space-y-4">
        {filteredExercises.map(renderExercise)}
        {filteredExercises.length === 0 && (
          <p className="text-center text-text-secondary">No exercises available.</p>
        )}
      </div>
    </div>
  );
}
