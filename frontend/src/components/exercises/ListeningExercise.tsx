import { useState, useRef, useEffect } from 'react';
import { useCheckAnswer } from '../../hooks/useLesson';
import type { Exercise } from '../../types/lesson';

interface ListeningExerciseProps {
  exercise: Exercise;
  showSlug: string;
  episodeSlug: string;
}

type AnswerState = 'idle' | 'checking' | 'correct' | 'incorrect';
type AudioState = 'loading' | 'ready' | 'playing' | 'error';

export function ListeningExercise({ exercise, showSlug, episodeSlug }: ListeningExerciseProps) {
  const [answer, setAnswer] = useState('');
  const [answerState, setAnswerState] = useState<AnswerState>('idle');
  const [audioState, setAudioState] = useState<AudioState>('loading');
  const [correctAnswer, setCorrectAnswer] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement>(null);

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);

  // Parse audio URL from exercise.hint field (temporary until backend is updated)
  const audioUrl = exercise.hint || '';

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handleCanPlay = () => setAudioState('ready');
    const handleError = () => setAudioState('error');
    const handlePlay = () => setAudioState('playing');
    const handlePause = () => setAudioState('ready');
    const handleEnded = () => setAudioState('ready');

    audio.addEventListener('canplay', handleCanPlay);
    audio.addEventListener('error', handleError);
    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);
    audio.addEventListener('ended', handleEnded);

    return () => {
      audio.removeEventListener('canplay', handleCanPlay);
      audio.removeEventListener('error', handleError);
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
      audio.removeEventListener('ended', handleEnded);
    };
  }, []);

  const handlePlayPause = () => {
    const audio = audioRef.current;
    if (!audio) return;

    if (audioState === 'playing') {
      audio.pause();
    } else {
      audio.play();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!answer.trim() || answerState === 'checking') return;

    setAnswerState('checking');
    try {
      const result = await checkAnswerMutation.mutateAsync({
        exerciseId: exercise.id,
        answer: answer.trim(),
      });

      if (result.correct) {
        setAnswerState('correct');
      } else {
        setAnswerState('incorrect');
        setCorrectAnswer(result.correctAnswer);
      }
    } catch {
      setAnswerState('idle');
    }
  };

  const handleReset = () => {
    setAnswer('');
    setAnswerState('idle');
    setCorrectAnswer(null);
  };

  const getAudioButtonContent = () => {
    switch (audioState) {
      case 'loading':
        return (
          <>
            <svg className="h-6 w-6 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <span>Loading...</span>
          </>
        );
      case 'playing':
        return (
          <>
            <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z"
                clipRule="evenodd"
              />
            </svg>
            <span>Pause</span>
          </>
        );
      case 'error':
        return (
          <>
            <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            <span>Audio Error</span>
          </>
        );
      default:
        return (
          <>
            <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
                clipRule="evenodd"
              />
            </svg>
            <span>Play Audio</span>
          </>
        );
    }
  };

  return (
    <div className="rounded-xl border border-border bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-dark px-2 py-1 text-xs font-medium text-text-secondary">
          Listening Exercise
        </span>
        <span className="font-mono text-sm text-accent-primary">{exercise.points} pts</span>
      </div>

      <p className="my-4 text-lg text-text-primary">{exercise.question}</p>

      {/* Audio Player */}
      <div className="mb-4">
        <audio ref={audioRef} src={audioUrl} preload="auto" className="hidden" />
        <button
          onClick={handlePlayPause}
          disabled={audioState === 'loading' || audioState === 'error'}
          className={`flex w-full items-center justify-center gap-3 rounded-lg border p-4 transition-all ${
            audioState === 'error'
              ? 'border-error bg-error/10 text-error cursor-not-allowed'
              : audioState === 'playing'
                ? 'border-accent-primary bg-accent-primary/10 text-accent-primary hover:bg-accent-primary/20'
                : 'border-border bg-bg-dark text-text-primary hover:border-accent-primary hover:bg-bg-card-hover'
          } disabled:cursor-not-allowed disabled:opacity-50`}
        >
          {getAudioButtonContent()}
        </button>
      </div>

      {/* Answer Input */}
      {answerState === 'idle' || answerState === 'checking' ? (
        <form onSubmit={handleSubmit} className="flex gap-3">
          <input
            type="text"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Type what you heard..."
            className="flex-1 rounded-lg border border-border bg-bg-dark px-4 py-2 text-text-primary placeholder:text-text-secondary focus:border-accent-primary focus:outline-none"
            disabled={answerState === 'checking' || audioState === 'loading'}
          />
          <button
            type="submit"
            disabled={!answer.trim() || answerState === 'checking' || audioState === 'loading'}
            className="rounded-lg bg-accent-primary px-6 py-2 font-medium text-white transition-colors hover:bg-accent-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            {answerState === 'checking' ? 'Checking...' : 'Check'}
          </button>
        </form>
      ) : (
        <div>
          <div
            className={`mb-4 flex items-center gap-2 rounded-lg p-3 ${
              answerState === 'correct' ? 'bg-success/10 text-success' : 'bg-error/10 text-error'
            }`}
          >
            {answerState === 'correct' ? (
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
                <div>
                  <p className="mb-1">Incorrect.</p>
                  <p className="text-sm">
                    You wrote: <span className="font-mono text-error/80">{answer}</span>
                  </p>
                  <p className="text-sm">
                    Correct answer:{' '}
                    <strong className="font-mono text-text-primary">{correctAnswer}</strong>
                  </p>
                </div>
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
