import type { Vocabulary } from '../../types/lesson';
import { AudioPlayer } from '../shared/AudioPlayer';

interface VocabularyCardProps {
  vocabulary: Vocabulary;
}

const categoryColors: Record<string, string> = {
  MEDICAL: 'bg-red-900/30 text-red-400 border-red-800/50',
  TECHNICAL: 'bg-blue-900/30 text-blue-400 border-blue-800/50',
  SLANG: 'bg-purple-900/30 text-purple-400 border-purple-800/50',
  IDIOM: 'bg-yellow-900/30 text-yellow-400 border-yellow-800/50',
  PROFESSIONAL: 'bg-cyan-900/30 text-cyan-400 border-cyan-800/50',
  EVERYDAY: 'bg-green-900/30 text-green-400 border-green-800/50',
  EMOTIONAL: 'bg-pink-900/30 text-pink-400 border-pink-800/50',
  COLLOQUIAL: 'bg-orange-900/30 text-orange-400 border-orange-800/50',
  ACTION: 'bg-indigo-900/30 text-indigo-400 border-indigo-800/50',
};

const categoryLabels: Record<string, string> = {
  MEDICAL: 'Medical',
  TECHNICAL: 'Technical',
  SLANG: 'Slang',
  IDIOM: 'Idiom',
  PROFESSIONAL: 'Professional',
  EVERYDAY: 'Everyday',
  EMOTIONAL: 'Emotional',
  COLLOQUIAL: 'Colloquial',
  ACTION: 'Action',
};

export function VocabularyCard({ vocabulary }: VocabularyCardProps) {
  const categoryColor = categoryColors[vocabulary.category] || categoryColors.EVERYDAY;
  const categoryLabel = categoryLabels[vocabulary.category] || vocabulary.category;

  return (
    <div className="rounded-xl border border-border bg-bg-card p-5 transition-colors hover:border-accent-primary/30">
      <div className="mb-3 flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <h4 className="text-lg font-semibold text-text-primary">{vocabulary.term}</h4>
          {vocabulary.phonetic && (
            <p className="font-mono text-sm text-text-secondary">{vocabulary.phonetic}</p>
          )}
        </div>
        <div className="flex items-center gap-2">
          <span className={`rounded-md border px-2 py-1 text-xs ${categoryColor}`}>
            {categoryLabel}
          </span>
          <AudioPlayer
            src={vocabulary.audioUrl || ''}
            fallbackText={vocabulary.term}
            size="sm"
          />
        </div>
      </div>

      <p className="text-text-primary">{vocabulary.definition}</p>

      {vocabulary.exampleSentence && (
        <div className="mt-4 rounded-lg bg-bg-dark p-3">
          <p className="text-sm italic text-text-secondary">"{vocabulary.exampleSentence}"</p>
        </div>
      )}

      {vocabulary.contextTimestamp && (
        <p className="mt-3 font-mono text-xs text-text-secondary">
          Used at: {vocabulary.contextTimestamp}
        </p>
      )}
    </div>
  );
}
