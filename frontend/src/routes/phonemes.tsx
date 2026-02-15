import { useState, useEffect, useRef, useCallback } from 'react';
import { useSpeechSynthesis } from '../hooks/useSpeechSynthesis';
import {
  PHONEMES,
  CATEGORY_LABELS,
  CATEGORY_ORDER,
  type PhonemeCategory,
  type Phoneme,
} from '../data/phonemes';

type FilterCategory = 'all' | PhonemeCategory;

const FILTER_OPTIONS: { value: FilterCategory; label: string }[] = [
  { value: 'all', label: 'All' },
  ...CATEGORY_ORDER.map((cat) => ({
    value: cat as FilterCategory,
    label: CATEGORY_LABELS[cat],
  })),
];

function PhonemeCard({
  phoneme,
  highlighted,
  onSpeak,
  isSpeaking,
}: {
  phoneme: Phoneme;
  highlighted: boolean;
  onSpeak: (text: string) => void;
  isSpeaking: boolean;
}) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (highlighted && ref.current) {
      ref.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [highlighted]);

  return (
    <div
      ref={ref}
      id={phoneme.symbol}
      className={`rounded-xl border p-5 transition-all ${
        highlighted
          ? 'animate-pulse border-brand bg-brand-muted ring-2 ring-brand/30'
          : 'border-edge-default bg-bg-card hover:border-brand/30'
      }`}
    >
      <div className="mb-3 flex items-start justify-between gap-3">
        <div>
          <span className="font-mono text-2xl text-brand">{phoneme.symbol}</span>
          <p className="mt-1 text-sm text-content-secondary">{phoneme.name}</p>
        </div>
        <span className="shrink-0 rounded-full bg-bg-inset px-2.5 py-0.5 text-xs text-content-secondary">
          {CATEGORY_LABELS[phoneme.category]}
        </span>
      </div>

      {/* Keywords with speak buttons */}
      <div className="mb-3 flex flex-wrap gap-2">
        {phoneme.keywords.map((word) => (
          <button
            key={word}
            onClick={() => onSpeak(word)}
            disabled={isSpeaking}
            className="group flex min-h-[44px] items-center gap-1.5 rounded-lg border border-edge-default bg-bg-inset px-3 py-2 text-sm transition-colors hover:border-brand/50 hover:text-brand active:bg-brand/10 disabled:opacity-50"
          >
            <svg
              className="h-3.5 w-3.5 text-content-secondary group-hover:text-brand"
              fill="currentColor"
              viewBox="0 0 24 24"
            >
              <path d="M8 5v14l11-7z" />
            </svg>
            <span className="text-content-primary">{word}</span>
          </button>
        ))}
      </div>

      {/* Description */}
      <p className="mb-3 text-sm leading-relaxed text-content-secondary">{phoneme.description}</p>

      {/* Spanish tip */}
      <div className="rounded-lg border border-warning/20 bg-warning/5 px-3 py-2">
        <p className="text-sm text-content-secondary">
          <span className="font-medium text-warning">Para hispanohablantes:</span>{' '}
          {phoneme.spanishTip}
        </p>
      </div>
    </div>
  );
}

export function PhonemesPage() {
  const [filter, setFilter] = useState<FilterCategory>('all');
  const [highlightedSymbol, setHighlightedSymbol] = useState<string | null>(null);
  const tts = useSpeechSynthesis();

  const speak = useCallback(
    (text: string) => {
      tts.speak(text);
    },
    [tts],
  );

  // Handle hash-based deep linking
  useEffect(() => {
    const hash = window.location.hash.slice(1); // remove #
    if (hash) {
      const decoded = decodeURIComponent(hash);
      const phoneme = PHONEMES.find((p) => p.symbol === decoded);
      if (phoneme) {
        setFilter('all');
        setHighlightedSymbol(decoded);
        // Clear highlight after animation
        const timeout = setTimeout(() => setHighlightedSymbol(null), 3000);
        return () => clearTimeout(timeout);
      }
    }
  }, []);

  const filtered =
    filter === 'all' ? PHONEMES : PHONEMES.filter((p) => p.category === filter);

  // Group by category for display
  const grouped = CATEGORY_ORDER.reduce(
    (acc, category) => {
      const items = filtered.filter((p) => p.category === category);
      if (items.length > 0) {
        acc.push({ category, items });
      }
      return acc;
    },
    [] as { category: PhonemeCategory; items: Phoneme[] }[],
  );

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-content-primary">Phoneme Reference</h1>
        <p className="mt-1 text-content-secondary">
          {PHONEMES.length} English phonemes with pronunciation guides for Spanish speakers
        </p>
      </div>

      {/* Category Filter Tabs */}
      <div className="-mx-4 mb-8 overflow-x-auto px-4 sm:mx-0 sm:px-0">
        <div className="flex gap-2 sm:flex-wrap">
          {FILTER_OPTIONS.map((option) => (
            <button
              key={option.value}
              onClick={() => setFilter(option.value)}
              className={`shrink-0 rounded-lg px-4 py-2 text-sm font-medium transition-colors active:scale-95 ${
                filter === option.value
                  ? 'bg-brand text-white'
                  : 'bg-bg-card text-content-secondary hover:bg-bg-inset hover:text-content-primary'
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* Phoneme Cards grouped by category */}
      <div className="space-y-10">
        {grouped.map(({ category, items }) => (
          <section key={category}>
            <h2 className="mb-4 text-lg font-semibold text-content-primary">
              {CATEGORY_LABELS[category]}
              <span className="ml-2 text-sm font-normal text-content-secondary">
                ({items.length})
              </span>
            </h2>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {items.map((phoneme) => (
                <PhonemeCard
                  key={phoneme.symbol}
                  phoneme={phoneme}
                  highlighted={highlightedSymbol === phoneme.symbol}
                  onSpeak={speak}
                  isSpeaking={tts.state === 'speaking'}
                />
              ))}
            </div>
          </section>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="rounded-xl border border-edge-default bg-bg-card p-8 text-center">
          <p className="text-content-secondary">No phonemes match this filter.</p>
        </div>
      )}
    </div>
  );
}
