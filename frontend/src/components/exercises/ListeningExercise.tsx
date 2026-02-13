import { useState, useEffect, useCallback } from 'react';
import { useCheckAnswer } from '../../hooks/useLesson';
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis';
import type { Exercise } from '../../types/lesson';

interface ListeningExerciseProps {
  exercise: Exercise;
  showSlug: string;
  episodeSlug: string;
}

type AnswerState = 'idle' | 'checking' | 'correct' | 'incorrect';
type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;

const SPEEDS: PlaybackSpeed[] = [0.5, 0.75, 1, 1.25, 1.5];
const RECOMMENDED_LISTENS = 2;

function extractSpeechText(exercise: Exercise): string {
  // Use correctAnswer directly if available
  if (exercise.correctAnswer) return exercise.correctAnswer;

  // Try to extract from question brackets: [word]
  const bracketMatch = exercise.question.match(/\[([^\]]+)\]/);
  if (bracketMatch) return bracketMatch[1];

  // Try colon format: "Listen: word"
  const colonMatch = exercise.question.match(/:\s*(.+)$/);
  if (colonMatch) return colonMatch[1].trim();

  return exercise.question;
}

export function ListeningExercise({ exercise, showSlug, episodeSlug }: ListeningExerciseProps) {
  const [answer, setAnswer] = useState('');
  const [answerState, setAnswerState] = useState<AnswerState>('idle');
  const [correctAnswer, setCorrectAnswer] = useState<string | null>(null);
  const [playCount, setPlayCount] = useState(0);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);
  const tts = useSpeechSynthesis({
    onEnd: useCallback(() => setPlayCount((prev) => prev + 1), []),
  });

  const speechText = extractSpeechText(exercise);

  // Close speed menu when clicking outside
  useEffect(() => {
    if (!showSpeedMenu) return;
    const handleClickOutside = () => setShowSpeedMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showSpeedMenu]);

  const handlePlayPause = () => {
    if (tts.state === 'speaking') {
      tts.pause();
    } else if (tts.state === 'paused') {
      tts.resume();
    } else {
      tts.speak(speechText);
    }
  };

  const handleReplay = () => {
    tts.stop();
    tts.speak(speechText);
  };

  const handleSpeedChange = (newSpeed: PlaybackSpeed) => {
    tts.setRate(newSpeed);
    setShowSpeedMenu(false);
  };

  const toggleSpeedMenu = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowSpeedMenu(!showSpeedMenu);
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

  const needsMoreListening = playCount < RECOMMENDED_LISTENS && answerState === 'idle';

  return (
    <div className="rounded-xl border border-edge-default bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-inset px-2 py-1 text-xs font-medium text-content-secondary">
          Listening Exercise
        </span>
        <span className="font-mono text-sm text-brand">{exercise.points} pts</span>
      </div>

      <p className="my-4 text-lg text-content-primary">Listen and type what you hear</p>

      {/* Audio Player */}
      <div className="mb-4">
        <div className="flex items-center gap-2">
          {/* Play/Pause Button */}
          <button
            onClick={handlePlayPause}
            className={`flex flex-1 items-center justify-center gap-3 rounded-lg border p-4 transition-all ${
              tts.state === 'error'
                ? 'cursor-not-allowed border-error bg-error/10 text-error'
                : tts.state === 'speaking'
                  ? 'border-brand bg-brand-muted text-brand hover:bg-brand-muted'
                  : 'border-edge-default bg-bg-inset text-content-primary hover:border-brand hover:bg-bg-card-hover'
            }`}
          >
            {tts.state === 'error' ? (
              <>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clipRule="evenodd"
                  />
                </svg>
                <span>Speech Error</span>
              </>
            ) : tts.state === 'speaking' ? (
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
            ) : (
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
            )}
          </button>

          {/* Replay Button */}
          {playCount > 0 && tts.state !== 'error' && (
            <button
              onClick={handleReplay}
              className="flex h-14 w-14 items-center justify-center rounded-lg border border-edge-default bg-bg-inset text-content-secondary transition-colors hover:border-brand hover:text-brand"
              title="Replay from start"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
            </button>
          )}

          {/* Speed Control */}
          <div className="relative">
            <button
              onClick={toggleSpeedMenu}
              className="flex h-14 items-center justify-center rounded-lg border border-edge-default bg-bg-inset px-3 font-mono text-sm text-content-secondary transition-colors hover:border-brand hover:text-brand"
              title="Playback speed"
            >
              {tts.rate}x
            </button>

            {showSpeedMenu && (
              <div className="absolute bottom-full right-0 z-10 mb-1 rounded-lg border border-edge-default bg-bg-card py-1 shadow-lg">
                {SPEEDS.map((s) => (
                  <button
                    key={s}
                    onClick={() => handleSpeedChange(s)}
                    className={`block w-full px-4 py-1.5 text-left font-mono text-sm transition-colors hover:bg-bg-inset ${
                      tts.rate === s ? 'text-brand' : 'text-content-secondary'
                    }`}
                  >
                    {s}x {s === 0.5 && '(slow)'} {s === 1 && '(normal)'}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Play count and hint */}
        <div className="mt-2 flex items-center justify-between text-xs">
          <span className="text-content-secondary">
            Played: <span className="font-mono text-content-primary">{playCount}</span> time
            {playCount !== 1 ? 's' : ''}
          </span>
          {needsMoreListening && (
            <span className="text-warning">
              Listen at least {RECOMMENDED_LISTENS} times before answering
            </span>
          )}
          {tts.rate !== 1 && (
            <span className="text-brand">Speed: {tts.rate}x</span>
          )}
        </div>
      </div>

      {/* Answer Input */}
      {answerState === 'idle' || answerState === 'checking' ? (
        <form onSubmit={handleSubmit} className="flex gap-3">
          <input
            type="text"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Type what you heard..."
            className="flex-1 rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-content-primary placeholder:text-content-secondary focus:border-brand focus:outline-none"
            disabled={answerState === 'checking'}
          />
          <button
            type="submit"
            disabled={!answer.trim() || answerState === 'checking'}
            className="rounded-lg bg-brand px-6 py-2 font-medium text-white transition-colors hover:bg-brand-hover disabled:cursor-not-allowed disabled:opacity-50"
          >
            {answerState === 'checking' ? 'Checking...' : 'Check'}
          </button>
        </form>
      ) : (
        <div>
          <div
            className={`mb-4 flex items-start gap-3 rounded-lg p-4 ${
              answerState === 'correct'
                ? 'bg-success/10 border border-success/30'
                : 'bg-error/10 border border-error/30'
            }`}
          >
            {answerState === 'correct' ? (
              <>
                <svg
                  className="mt-0.5 h-5 w-5 shrink-0 text-success"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                <div>
                  <p className="font-medium text-success">Correct! +{exercise.points} points</p>
                  <p className="mt-1 text-sm text-content-secondary">
                    Great listening skills! You heard it correctly.
                  </p>
                </div>
              </>
            ) : (
              <>
                <svg
                  className="mt-0.5 h-5 w-5 shrink-0 text-error"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clipRule="evenodd"
                  />
                </svg>
                <div className="min-w-0 flex-1">
                  <p className="font-medium text-error">Not quite right</p>
                  <div className="mt-2 space-y-1 text-sm">
                    <p className="text-content-secondary">
                      You wrote:{' '}
                      <span className="rounded bg-error/20 px-1.5 py-0.5 font-mono text-error">
                        {answer}
                      </span>
                    </p>
                    <p className="text-content-secondary">
                      Correct:{' '}
                      <span className="rounded bg-success/20 px-1.5 py-0.5 font-mono text-success">
                        {correctAnswer}
                      </span>
                    </p>
                  </div>
                </div>
              </>
            )}
          </div>

          <div className="flex gap-2">
            <button
              onClick={handleReset}
              className="rounded-lg border border-edge-default bg-bg-inset px-4 py-2 text-sm text-content-secondary transition-colors hover:bg-bg-card-hover hover:text-content-primary"
            >
              Try Again
            </button>
            {answerState === 'incorrect' && (
              <button
                onClick={handleReplay}
                className="rounded-lg border border-brand/50 bg-brand-muted px-4 py-2 text-sm text-brand transition-colors hover:bg-brand-muted"
              >
                Listen Again
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
