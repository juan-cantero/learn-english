import { useState } from 'react';
import { useCheckAnswer } from '../../hooks/useLesson';
import type { Exercise } from '../../types/lesson';

interface FillInBlankProps {
  exercise: Exercise;
  showSlug: string;
  episodeSlug: string;
}

type AnswerState = 'idle' | 'checking' | 'correct' | 'incorrect';

export function FillInBlank({ exercise, showSlug, episodeSlug }: FillInBlankProps) {
  const [answer, setAnswer] = useState('');
  const [state, setState] = useState<AnswerState>('idle');
  const [correctAnswer, setCorrectAnswer] = useState<string | null>(null);

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!answer.trim() || state === 'checking') return;

    setState('checking');
    try {
      const result = await checkAnswerMutation.mutateAsync({
        exerciseId: exercise.id,
        answer: answer.trim(),
      });

      if (result.correct) {
        setState('correct');
      } else {
        setState('incorrect');
        setCorrectAnswer(result.correctAnswer);
      }
    } catch {
      setState('idle');
    }
  };

  const handleReset = () => {
    setAnswer('');
    setState('idle');
    setCorrectAnswer(null);
  };

  const questionParts = exercise.question.split('___');

  return (
    <div className="rounded-xl border border-border bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-dark px-2 py-1 text-xs font-medium text-text-secondary">
          Fill in the Blank
        </span>
        <span className="font-mono text-sm text-accent-primary">{exercise.points} pts</span>
      </div>

      <div className="my-4 text-lg text-text-primary">
        {questionParts.map((part, index) => (
          <span key={index}>
            {part}
            {index < questionParts.length - 1 && (
              <span
                className={`mx-1 inline-block min-w-[100px] border-b-2 px-2 ${
                  state === 'correct'
                    ? 'border-success text-success'
                    : state === 'incorrect'
                      ? 'border-error text-error'
                      : 'border-text-secondary'
                }`}
              >
                {state === 'correct' || state === 'incorrect'
                  ? answer
                  : answer || exercise.hint || '______'}
              </span>
            )}
          </span>
        ))}
      </div>

      {state === 'idle' || state === 'checking' ? (
        <form onSubmit={handleSubmit} className="flex gap-3">
          <input
            type="text"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Type your answer..."
            className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary placeholder:text-text-secondary focus:border-accent-primary focus:outline-none"
            disabled={state === 'checking'}
          />
          <button
            type="submit"
            disabled={!answer.trim() || state === 'checking'}
            className="rounded-lg bg-accent-primary px-6 py-2 font-medium text-white transition-colors hover:bg-accent-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            {state === 'checking' ? 'Checking...' : 'Check'}
          </button>
        </form>
      ) : (
        <div>
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
                <span>
                  Incorrect. The correct answer is:{' '}
                  <strong className="text-text-primary">{correctAnswer}</strong>
                </span>
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
