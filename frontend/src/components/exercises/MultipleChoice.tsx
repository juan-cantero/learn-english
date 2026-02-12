import { useState, useMemo } from 'react';
import { useCheckAnswer } from '../../hooks/useLesson';
import type { Exercise } from '../../types/lesson';

interface MultipleChoiceProps {
  exercise: Exercise;
  showSlug: string;
  episodeSlug: string;
}

type AnswerState = 'idle' | 'checking' | 'correct' | 'incorrect';

export function MultipleChoice({ exercise, showSlug, episodeSlug }: MultipleChoiceProps) {
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [state, setState] = useState<AnswerState>('idle');
  const [correctAnswer, setCorrectAnswer] = useState<string | null>(null);

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);

  const options = useMemo(() => {
    if (!exercise.options) return [];
    try {
      return JSON.parse(exercise.options) as string[];
    } catch {
      return [];
    }
  }, [exercise.options]);

  const handleSelect = async (option: string) => {
    if (state !== 'idle') return;

    setSelectedOption(option);
    setState('checking');

    try {
      const result = await checkAnswerMutation.mutateAsync({
        exerciseId: exercise.id,
        answer: option,
      });

      if (result.correct) {
        setState('correct');
      } else {
        setState('incorrect');
        setCorrectAnswer(result.correctAnswer);
      }
    } catch {
      setState('idle');
      setSelectedOption(null);
    }
  };

  const handleReset = () => {
    setSelectedOption(null);
    setState('idle');
    setCorrectAnswer(null);
  };

  const getOptionStyle = (option: string) => {
    if (state === 'idle' || state === 'checking') {
      return selectedOption === option
        ? 'border-brand bg-brand-muted'
        : 'border-edge-default hover:border-brand/50 hover:bg-bg-card-hover';
    }

    if (state === 'correct' && selectedOption === option) {
      return 'border-success bg-success/10';
    }

    if (state === 'incorrect') {
      if (selectedOption === option) {
        return 'border-error bg-error/10';
      }
      if (option === correctAnswer) {
        return 'border-success bg-success/10';
      }
    }

    return 'border-edge-default opacity-50';
  };

  return (
    <div className="rounded-xl border border-edge-default bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-inset px-2 py-1 text-xs font-medium text-content-secondary">
          Multiple Choice
        </span>
        <span className="font-mono text-sm text-brand">{exercise.points} pts</span>
      </div>

      <p className="my-4 text-lg text-content-primary">{exercise.question}</p>

      <div className="space-y-2">
        {options.map((option, index) => (
          <button
            key={index}
            onClick={() => handleSelect(option)}
            disabled={state !== 'idle'}
            className={`flex w-full items-center gap-3 rounded-lg border p-4 text-left transition-all ${getOptionStyle(option)} ${state !== 'idle' ? 'cursor-default' : 'cursor-pointer'}`}
          >
            <span
              className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full border text-sm font-medium ${
                selectedOption === option
                  ? state === 'correct'
                    ? 'border-success bg-success text-white'
                    : state === 'incorrect'
                      ? 'border-error bg-error text-white'
                      : 'border-brand bg-brand text-white'
                  : 'border-content-secondary text-content-secondary'
              }`}
            >
              {String.fromCharCode(65 + index)}
            </span>
            <span className="text-content-primary">{option}</span>
            {state === 'correct' && selectedOption === option && (
              <svg className="ml-auto h-5 w-5 text-success" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            )}
            {state === 'incorrect' && selectedOption === option && (
              <svg className="ml-auto h-5 w-5 text-error" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            )}
            {state === 'incorrect' && option === correctAnswer && (
              <svg className="ml-auto h-5 w-5 text-success" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
            )}
          </button>
        ))}
      </div>

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
                <span className="font-medium">Correct! +{exercise.points} points</span>
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
                <span>Incorrect. See the correct answer above.</span>
              </>
            )}
          </div>
          <button
            onClick={handleReset}
            className="rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-sm text-content-secondary transition-colors hover:bg-bg-card-hover hover:text-content-primary"
          >
            Try Again
          </button>
        </div>
      )}
    </div>
  );
}
