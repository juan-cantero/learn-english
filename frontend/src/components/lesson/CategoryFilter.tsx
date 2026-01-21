import type { VocabularyCategory } from '../../types/lesson';

interface CategoryFilterProps {
  categories: VocabularyCategory[];
  selected: VocabularyCategory | 'ALL';
  onSelect: (category: VocabularyCategory | 'ALL') => void;
  counts: Record<VocabularyCategory | 'ALL', number>;
}

const categoryLabels: Record<VocabularyCategory | 'ALL', string> = {
  ALL: 'All',
  MEDICAL: 'Medical',
  TECHNICAL: 'Technical',
  SLANG: 'Slang',
  IDIOM: 'Idioms',
  PROFESSIONAL: 'Professional',
  EVERYDAY: 'Everyday',
};

export function CategoryFilter({ categories, selected, onSelect, counts }: CategoryFilterProps) {
  const allCategories: (VocabularyCategory | 'ALL')[] = ['ALL', ...categories];

  return (
    <div className="flex flex-wrap gap-2">
      {allCategories.map((category) => (
        <button
          key={category}
          onClick={() => onSelect(category)}
          className={`rounded-lg px-4 py-2 text-sm font-medium transition-all ${
            selected === category
              ? 'bg-accent-primary text-white'
              : 'bg-bg-card text-text-secondary hover:bg-bg-card-hover hover:text-text-primary'
          }`}
        >
          {categoryLabels[category]}
          <span className="ml-2 font-mono text-xs opacity-70">{counts[category]}</span>
        </button>
      ))}
    </div>
  );
}
