import { useState, useMemo } from 'react';
import { useParams } from '@tanstack/react-router';
import { useLesson } from '../../hooks/useLesson';
import { Navigation } from '../../components/layout/Navigation';
import { ProgressBar } from '../../components/layout/ProgressBar';
import { VocabularyCard } from '../../components/lesson/VocabularyCard';
import { GrammarCard } from '../../components/lesson/GrammarCard';
import { ExpressionCard } from '../../components/lesson/ExpressionCard';
import { CategoryFilter } from '../../components/lesson/CategoryFilter';
import { ExerciseSection } from '../../components/exercises/ExerciseSection';
import { PracticePronunciation } from '../../components/lesson/PracticePronunciation';
import type { VocabularyCategory } from '../../types/lesson';

type TabId = 'vocabulary' | 'grammar' | 'expressions' | 'exercises' | 'practice';

interface Tab {
  id: TabId;
  label: string;
  count: number;
}

export function LessonPage() {
  const { slug, episodeSlug } = useParams({ from: '/shows/$slug/episodes/$episodeSlug' });
  const { data: lesson, isLoading, error } = useLesson(slug, episodeSlug);

  const [activeTab, setActiveTab] = useState<TabId>('vocabulary');
  const [selectedCategory, setSelectedCategory] = useState<VocabularyCategory | 'ALL'>('ALL');

  const audioItemsCount = useMemo(() => {
    if (!lesson) return 0;
    return (
      lesson.vocabulary.filter((v) => v.audioUrl).length +
      lesson.expressions.filter((e) => e.audioUrl).length
    );
  }, [lesson]);

  const tabs: Tab[] = useMemo(() => {
    if (!lesson) return [];
    return [
      { id: 'vocabulary', label: 'Vocabulary', count: lesson.vocabulary.length },
      { id: 'grammar', label: 'Grammar', count: lesson.grammarPoints.length },
      { id: 'expressions', label: 'Expressions', count: lesson.expressions.length },
      { id: 'exercises', label: 'Exercises', count: lesson.exercises.length },
      { id: 'practice', label: 'Practice', count: audioItemsCount },
    ];
  }, [lesson, audioItemsCount]);

  const vocabularyCategories = useMemo(() => {
    if (!lesson) return [];
    const cats = new Set(lesson.vocabulary.map((v) => v.category));
    return Array.from(cats) as VocabularyCategory[];
  }, [lesson]);

  const categoryCounts = useMemo(() => {
    if (!lesson) return { ALL: 0 } as Record<VocabularyCategory | 'ALL', number>;
    const counts: Record<VocabularyCategory | 'ALL', number> = { ALL: lesson.vocabulary.length } as any;
    for (const cat of vocabularyCategories) {
      counts[cat] = lesson.vocabulary.filter((v) => v.category === cat).length;
    }
    return counts;
  }, [lesson, vocabularyCategories]);

  const filteredVocabulary = useMemo(() => {
    if (!lesson) return [];
    if (selectedCategory === 'ALL') return lesson.vocabulary;
    return lesson.vocabulary.filter((v) => v.category === selectedCategory);
  }, [lesson, selectedCategory]);

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-4 h-4 w-24 rounded bg-bg-card" />
          <div className="mb-6 h-8 w-2/3 rounded bg-bg-card" />
          <div className="mb-8 h-2 w-full rounded bg-bg-card" />
          <div className="flex gap-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-10 w-24 rounded bg-bg-card" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-error/50 bg-error/10 p-4">
          <p className="text-error">Failed to load lesson: {error.message}</p>
        </div>
      </div>
    );
  }

  if (!lesson) return null;

  const { episode, progress } = lesson;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <Navigation
        showTitle={episode.showSlug}
        showSlug={episode.showSlug}
        episodeTitle={episode.title}
      />

      <div className="mt-6">
        <div className="mb-2 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-text-primary">{episode.title}</h1>
          <span className="rounded-lg bg-bg-card px-3 py-1 font-mono text-sm text-text-secondary">
            S{episode.seasonNumber.toString().padStart(2, '0')}E
            {episode.episodeNumber.toString().padStart(2, '0')}
          </span>
        </div>

        {episode.synopsis && (
          <p className="mb-4 text-text-secondary">{episode.synopsis}</p>
        )}

        <div className="mb-6 rounded-lg border border-border bg-bg-card p-4">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">Lesson Progress</span>
            <span className="font-mono text-sm text-text-primary">
              {progress.earnedPoints} / {progress.totalPoints} pts
            </span>
          </div>
          <ProgressBar percentage={progress.completionPercentage} size="lg" />
          {progress.isComplete && (
            <div className="mt-3 flex items-center gap-2 text-success">
              <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clipRule="evenodd"
                />
              </svg>
              <span className="text-sm font-medium">Lesson Complete!</span>
            </div>
          )}
        </div>
      </div>

      <div className="mb-6 border-b border-border">
        <div className="flex gap-1">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`relative px-4 py-3 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'text-accent-primary'
                  : 'text-text-secondary hover:text-text-primary'
              }`}
            >
              {tab.label}
              <span className="ml-2 font-mono text-xs opacity-70">{tab.count}</span>
              {activeTab === tab.id && (
                <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-accent-primary" />
              )}
            </button>
          ))}
        </div>
      </div>

      <div className="min-h-[400px]">
        {activeTab === 'vocabulary' && (
          <div>
            <div className="mb-6">
              <CategoryFilter
                categories={vocabularyCategories}
                selected={selectedCategory}
                onSelect={setSelectedCategory}
                counts={categoryCounts}
              />
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              {filteredVocabulary.map((vocab) => (
                <VocabularyCard key={vocab.id} vocabulary={vocab} />
              ))}
            </div>
            {filteredVocabulary.length === 0 && (
              <p className="text-center text-text-secondary">
                No vocabulary items in this category.
              </p>
            )}
          </div>
        )}

        {activeTab === 'grammar' && (
          <div className="grid gap-4 md:grid-cols-2">
            {lesson.grammarPoints.map((gp) => (
              <GrammarCard key={gp.id} grammarPoint={gp} />
            ))}
            {lesson.grammarPoints.length === 0 && (
              <p className="col-span-2 text-center text-text-secondary">
                No grammar points for this lesson.
              </p>
            )}
          </div>
        )}

        {activeTab === 'expressions' && (
          <div className="grid gap-4 md:grid-cols-2">
            {lesson.expressions.map((expr) => (
              <ExpressionCard key={expr.id} expression={expr} />
            ))}
            {lesson.expressions.length === 0 && (
              <p className="col-span-2 text-center text-text-secondary">
                No expressions for this lesson.
              </p>
            )}
          </div>
        )}

        {activeTab === 'exercises' && (
          <ExerciseSection
            exercises={lesson.exercises}
            showSlug={slug}
            episodeSlug={episodeSlug}
          />
        )}

        {activeTab === 'practice' && (
          <PracticePronunciation
            vocabulary={lesson.vocabulary}
            expressions={lesson.expressions}
          />
        )}
      </div>
    </div>
  );
}
