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
type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;

const SPEEDS: PlaybackSpeed[] = [0.5, 0.75, 1, 1.25, 1.5];
const RECOMMENDED_LISTENS = 2;

export function ListeningExercise({ exercise, showSlug, episodeSlug }: ListeningExerciseProps) {
  const [answer, setAnswer] = useState('');
  const [answerState, setAnswerState] = useState<AnswerState>('idle');
  const [audioState, setAudioState] = useState<AudioState>('loading');
  const [correctAnswer, setCorrectAnswer] = useState<string | null>(null);
  const [playCount, setPlayCount] = useState(0);
  const [speed, setSpeed] = useState<PlaybackSpeed>(1);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);
  const audioRef = useRef<HTMLAudioElement>(null);

  const checkAnswerMutation = useCheckAnswer(showSlug, episodeSlug);

  // Use stored audioUrl, or fallback to on-demand TTS
  const getAudioUrl = (): string => {
    // Prefer stored audio URL from backend
    if (exercise.audioUrl) {
      return exercise.audioUrl;
    }

    // Fallback: extract word from question and use TTS
    const extractWord = (question: string): string => {
      const bracketMatch = question.match(/\[([^\]]+)\]/);
      if (bracketMatch) return bracketMatch[1];
      const colonMatch = question.match(/:\s*(.+)$/);
      if (colonMatch) return colonMatch[1].trim();
      return '';
    };

    const word = extractWord(exercise.question);
    return word
      ? `http://localhost:8080/api/v1/tts/speak?text=${encodeURIComponent(word)}`
      : '';
  };

  const audioUrl = getAudioUrl();

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handleCanPlay = () => setAudioState('ready');
    const handleError = () => setAudioState('error');
    const handlePlay = () => setAudioState('playing');
    const handlePause = () => setAudioState('ready');
    const handleEnded = () => {
      setAudioState('ready');
      setPlayCount((prev) => prev + 1);
    };

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

  // Close speed menu when clicking outside
  useEffect(() => {
    if (!showSpeedMenu) return;
    const handleClickOutside = () => setShowSpeedMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showSpeedMenu]);

  const handlePlayPause = () => {
    const audio = audioRef.current;
    if (!audio) return;

    if (audioState === 'playing') {
      audio.pause();
    } else {
      audio.playbackRate = speed;
      audio.play();
    }
  };

  const handleReplay = () => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.currentTime = 0;
    audio.playbackRate = speed;
    audio.play();
  };

  const handleSpeedChange = (newSpeed: PlaybackSpeed) => {
    setSpeed(newSpeed);
    if (audioRef.current) {
      audioRef.current.playbackRate = newSpeed;
    }
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
    <div className="rounded-xl border border-border bg-bg-card p-5">
      <div className="mb-1 flex items-center justify-between">
        <span className="rounded bg-bg-dark px-2 py-1 text-xs font-medium text-text-secondary">
          Listening Exercise
        </span>
        <span className="font-mono text-sm text-accent-primary">{exercise.points} pts</span>
      </div>

      <p className="my-4 text-lg text-text-primary">Listen and type what you hear</p>

      {/* Audio Player */}
      <div className="mb-4">
        <audio ref={audioRef} src={audioUrl} preload="auto" className="hidden" />

        <div className="flex items-center gap-2">
          {/* Play/Pause Button */}
          <button
            onClick={handlePlayPause}
            disabled={audioState === 'loading' || audioState === 'error'}
            className={`flex flex-1 items-center justify-center gap-3 rounded-lg border p-4 transition-all ${
              audioState === 'error'
                ? 'cursor-not-allowed border-error bg-error/10 text-error'
                : audioState === 'playing'
                  ? 'border-accent-primary bg-accent-primary/10 text-accent-primary hover:bg-accent-primary/20'
                  : 'border-border bg-bg-dark text-text-primary hover:border-accent-primary hover:bg-bg-card-hover'
            } disabled:cursor-not-allowed disabled:opacity-50`}
          >
            {audioState === 'loading' ? (
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
            ) : audioState === 'error' ? (
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
            ) : audioState === 'playing' ? (
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
          {playCount > 0 && audioState !== 'loading' && audioState !== 'error' && (
            <button
              onClick={handleReplay}
              className="flex h-14 w-14 items-center justify-center rounded-lg border border-border bg-bg-dark text-text-secondary transition-colors hover:border-accent-primary hover:text-accent-primary"
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
              className="flex h-14 items-center justify-center rounded-lg border border-border bg-bg-dark px-3 font-mono text-sm text-text-secondary transition-colors hover:border-accent-primary hover:text-accent-primary"
              title="Playback speed"
            >
              {speed}x
            </button>

            {showSpeedMenu && (
              <div className="absolute bottom-full right-0 z-10 mb-1 rounded-lg border border-border bg-bg-card py-1 shadow-lg">
                {SPEEDS.map((s) => (
                  <button
                    key={s}
                    onClick={() => handleSpeedChange(s)}
                    className={`block w-full px-4 py-1.5 text-left font-mono text-sm transition-colors hover:bg-bg-dark ${
                      speed === s ? 'text-accent-primary' : 'text-text-secondary'
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
          <span className="text-text-secondary">
            Played: <span className="font-mono text-text-primary">{playCount}</span> time
            {playCount !== 1 ? 's' : ''}
          </span>
          {needsMoreListening && (
            <span className="text-yellow-500">
              Listen at least {RECOMMENDED_LISTENS} times before answering
            </span>
          )}
          {speed !== 1 && (
            <span className="text-accent-primary">Speed: {speed}x</span>
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
                  <p className="mt-1 text-sm text-text-secondary">
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
                    <p className="text-text-secondary">
                      You wrote:{' '}
                      <span className="rounded bg-error/20 px-1.5 py-0.5 font-mono text-error">
                        {answer}
                      </span>
                    </p>
                    <p className="text-text-secondary">
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
              className="rounded-lg border border-border bg-bg-dark px-4 py-2 text-sm text-text-secondary transition-colors hover:bg-bg-card-hover hover:text-text-primary"
            >
              Try Again
            </button>
            {answerState === 'incorrect' && (
              <button
                onClick={handleReplay}
                className="rounded-lg border border-accent-primary/50 bg-accent-primary/10 px-4 py-2 text-sm text-accent-primary transition-colors hover:bg-accent-primary/20"
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
