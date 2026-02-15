import type { Vocabulary } from '../../types/lesson';
import { AudioPlayer } from '../shared/AudioPlayer';
import { PhonemeTooltip } from './PhonemeTooltip';

interface VocabularyCardProps {
  vocabulary: Vocabulary;
}

const categoryColors: Record<string, string> = {
  MEDICAL: 'bg-cat-medical-bg text-cat-medical border-cat-medical/30',
  TECHNICAL: 'bg-cat-technical-bg text-cat-technical border-cat-technical/30',
  SLANG: 'bg-cat-emotional-bg text-cat-emotional border-cat-emotional/30',
  IDIOM: 'bg-cat-general-bg text-cat-general border-cat-general/30',
  PROFESSIONAL: 'bg-cat-workplace-bg text-cat-workplace border-cat-workplace/30',
  EVERYDAY: 'bg-cat-informal-bg text-cat-informal border-cat-informal/30',
  EMOTIONAL: 'bg-cat-academic-bg text-cat-academic border-cat-academic/30',
  COLLOQUIAL: 'bg-cat-emergency-bg text-cat-emergency border-cat-emergency/30',
  ACTION: 'bg-cat-legal-bg text-cat-legal border-cat-legal/30',
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
    <div className="rounded-xl border border-edge-default bg-bg-card p-5 shadow-sm transition-colors hover:border-brand/30">
      <div className="mb-3 flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <h4 className="text-lg font-semibold text-content-primary">{vocabulary.term}</h4>
          {vocabulary.phonetic && (
            <p className="font-mono text-sm text-content-secondary">{vocabulary.phonetic}</p>
          )}
          {vocabulary.phonemes && vocabulary.phonemes.length > 0 && (
            <div className="mt-1 flex flex-wrap gap-1">
              {vocabulary.phonemes.map((phoneme) => (
                <PhonemeTooltip key={phoneme} symbol={phoneme} />
              ))}
            </div>
          )}
        </div>
        <div className="flex items-center gap-2">
          <span className={`rounded-md border px-2 py-1 text-xs ${categoryColor}`}>
            {categoryLabel}
          </span>
          <AudioPlayer text={vocabulary.term} size="sm" />
        </div>
      </div>

      <p className="text-content-primary">{vocabulary.definition}</p>

      {vocabulary.exampleSentence && (
        <div className="mt-4 rounded-lg bg-bg-inset p-3">
          <p className="text-sm italic text-content-secondary">"{vocabulary.exampleSentence}"</p>
        </div>
      )}

      {vocabulary.contextTimestamp && (
        <p className="mt-3 font-mono text-xs text-content-secondary">
          Used at: {vocabulary.contextTimestamp}
        </p>
      )}
    </div>
  );
}
