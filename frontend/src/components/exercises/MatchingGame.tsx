import { useState, useMemo } from 'react';
import { useCheckAnswer } from '../../hooks/useLesson';
import type { Exercise } from '../../types/lesson';

interface MatchingGameProps {
  exercise: Exercise;
  showSlug: string;
  episodeSlug: string;
}

interface MatchingPair {
  term: string;
  definition: string;
}

type AnswerState = 'idle' | 'checking' | 'correct' | 'incorrect';

export function MatchingGame({ exercise, showSlug, episodeSlug }: MatchingGameProps) {
  const [selectedTerm, setSelectedTerm] = useState<string | null>(null);
  const [matches, setMatches] = useState<Record<string, string>>({});
  const [state, setState] = useState<AnswerState>('idle');

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);

  const pairs = useMemo(() => {
    if (!exercise.matchingPairs) return [];
    try {
      return JSON.parse(exercise.matchingPairs) as MatchingPair[];
    } catch {
      return [];
    }
  }, [exercise.matchingPairs]);

  const shuffledDefinitions = useMemo(() => {
    return [...pairs].sort(() => Math.random() - 0.5).map((p) => p.definition);
  }, [pairs]);

  const handleTermClick = (term: string) => {
    if (state !== 'idle') return;
    setSelectedTerm(selectedTerm === term ? null : term);
  };

  const handleDefinitionClick = (definition: string) => {
    if (state !== 'idle' || !selectedTerm) return;

    const existingMatch = Object.entries(matches).find(([_, def]) => def === definition);
    if (existingMatch) {
      const newMatches = { ...matches };
      delete newMatches[existingMatch[0]];
      newMatches[selectedTerm] = definition;
      setMatches(newMatches);
    } else {
      setMatches({ ...matches, [selectedTerm]: definition });
    }
    setSelectedTerm(null);
  };

  const handleSubmit = async () => {
    if (Object.keys(matches).length !== pairs.length) return;

    setState('checking');

    const userMatches = pairs.map((pair) => ({
      term: pair.term,
      definition: matches[pair.term],
    }));

    try {
      const result = await checkAnswerMutation.mutateAsync({
        exerciseId: exercise.id,
        answer: JSON.stringify(userMatches),
      });

      setState(result.correct ? 'correct' : 'incorrect');
    } catch {
      setState('idle');
    }
  };

  const handleReset = () => {
    setSelectedTerm(null);
    setMatches({});
    setState('idle');
  };

  const getTermStyle = (term: string) => {
    const isMatched = matches[term];
    const isSelected = selectedTerm === term;

    if (state === 'correct' && isMatched) {
      return 'border-success bg-success/10 text-success';
    }
    if (state === 'incorrect' && isMatched) {
      const correctDef = pairs.find((p) => p.term === term)?.definition;
      return matches[term] === correctDef
        ? 'border-success bg-success/10 text-success'
        : 'border-error bg-error/10 text-error';
    }
    if (isSelected) {
      return 'border-accent-primary bg-accent-primary/10 text-accent-primary';
    }
    if (isMatched) {
      return 'border-accent-primary/50 bg-bg-card-hover text-text-primary';
    }
    return 'border-border hover:border-accent-primary/50 text-text-primary';
  };

  const getDefinitionStyle = (definition: string) => {
    const matchedTerm = Object.entries(matches).find(([_, def]) => def === definition)?.[0];

    if (state === 'correct' && matchedTerm) {
      return 'border-success bg-success/10 text-success';
    }
    if (state === 'incorrect' && matchedTerm) {
      const correctDef = pairs.find((p) => p.term === matchedTerm)?.definition;
      return definition === correctDef
        ? 'border-success bg-success/10 text-success'
        : 'border-error bg-error/10 text-error';
    }
    if (matchedTerm) {
      return 'border-accent-primary/50 bg-bg-card-hover text-text-primary';
    }
    if (selectedTerm) {
      return 'border-border hover:border-accent-primary cursor-pointer text-text-primary';
    }
    return 'border-border text-text-secondary';
  };

  const allMatched = Object.keys(matches).length === pairs.length;

  return (
    <div className="rounded-xl border border-border bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-dark px-2 py-1 text-xs font-medium text-text-secondary">
          Matching
        </span>
        <span className="font-mono text-sm text-accent-primary">{exercise.points} pts</span>
      </div>

      <p className="my-4 text-lg text-text-primary">{exercise.question}</p>

      <div className="mb-4 text-sm text-text-secondary">
        {state === 'idle' && !selectedTerm && 'Click a term, then click its matching definition.'}
        {state === 'idle' && selectedTerm && (
          <span>
            Selected: <strong className="text-accent-primary">{selectedTerm}</strong> — now click
            its definition
          </span>
        )}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-text-secondary">
            Terms
          </p>
          {pairs.map((pair) => (
            <button
              key={pair.term}
              onClick={() => handleTermClick(pair.term)}
              disabled={state !== 'idle'}
              className={`w-full rounded-lg border p-3 text-left transition-all ${getTermStyle(pair.term)}`}
            >
              {pair.term}
              {matches[pair.term] && (
                <span className="mt-1 block truncate text-xs opacity-70">
                  → {matches[pair.term]}
                </span>
              )}
            </button>
          ))}
        </div>

        <div className="space-y-2">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-text-secondary">
            Definitions
          </p>
          {shuffledDefinitions.map((definition, index) => (
            <button
              key={index}
              onClick={() => handleDefinitionClick(definition)}
              disabled={state !== 'idle' || !selectedTerm}
              className={`w-full rounded-lg border p-3 text-left transition-all ${getDefinitionStyle(definition)}`}
            >
              {definition}
            </button>
          ))}
        </div>
      </div>

      {state === 'idle' && (
        <div className="mt-4 flex gap-3">
          <button
            onClick={handleSubmit}
            disabled={!allMatched}
            className="rounded-lg bg-accent-primary px-6 py-2 font-medium text-white transition-colors hover:bg-accent-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            Check Answers
          </button>
          {Object.keys(matches).length > 0 && (
            <button
              onClick={handleReset}
              className="rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm text-text-secondary transition-colors hover:bg-bg-card-hover hover:text-text-primary"
            >
              Clear
            </button>
          )}
        </div>
      )}

      {(state === 'correct' || state === 'incorrect') && (
        <div className="mt-4">
          <div
            className={`mb-4 flex items-center gap-2 rounded-lg p-3 ${
              state === 'correct' ? 'bg-success/10 text-success' : 'bg-error/10 text-error'
            }`}
          >
            {state === 'correct' ? (
              <>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="font-medium">All matches correct! +{exercise.points} points</span>
              </>
            ) : (
              <>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clipRule="evenodd"
                  />
                </svg>
                <span>Some matches are incorrect. Try again!</span>
              </>
            )}
          </div>
          <button
            onClick={handleReset}
            className="rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm text-text-secondary transition-colors hover:bg-bg-card-hover hover:text-text-primary"
          >
            Try Again
          </button>
        </div>
      )}
    </div>
  );
}
