import { useState, useRef, useEffect, useCallback } from 'react';
import type { Vocabulary, Expression } from '../../types/lesson';

type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;
type PracticeItem = {
  id: string;
  text: string;
  audioUrl: string | null;
  type: 'vocabulary' | 'expression';
  definition: string;
};

interface PracticePronunciationProps {
  vocabulary: Vocabulary[];
  expressions: Expression[];
}

const SPEEDS: PlaybackSpeed[] = [0.5, 0.75, 1, 1.25, 1.5];
const REPEAT_OPTIONS = [1, 2, 3];

export function PracticePronunciation({ vocabulary, expressions }: PracticePronunciationProps) {
  // Convert vocabulary and expressions to practice items
  const allItems: PracticeItem[] = [
    ...vocabulary.map((v) => ({
      id: v.id,
      text: v.term,
      audioUrl: v.audioUrl,
      type: 'vocabulary' as const,
      definition: v.definition,
    })),
    ...expressions
      .filter((e) => e.audioUrl)
      .map((e) => ({
        id: e.id,
        text: e.phrase,
        audioUrl: e.audioUrl,
        type: 'expression' as const,
        definition: e.meaning,
      })),
  ].filter((item) => item.audioUrl);

  const [items, setItems] = useState<PracticeItem[]>(allItems);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [currentIndex, setCurrentIndex] = useState<number | null>(null);
  const [currentRepeat, setCurrentRepeat] = useState(1);
  const [speed, setSpeed] = useState<PlaybackSpeed>(1);
  const [repeatCount, setRepeatCount] = useState(2);
  const [isShuffled, setIsShuffled] = useState(false);
  const [practicedIds, setPracticedIds] = useState<Set<string>>(new Set());
  const [showSettings, setShowSettings] = useState(false);

  const audioRef = useRef<HTMLAudioElement | null>(null);
  const shouldStopRef = useRef(false);
  const isPausedRef = useRef(false);

  // Keep isPausedRef in sync
  useEffect(() => {
    isPausedRef.current = isPaused;
  }, [isPaused]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  const shuffleArray = <T,>(array: T[]): T[] => {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  };

  const toggleShuffle = () => {
    if (isShuffled) {
      setItems(allItems);
    } else {
      setItems(shuffleArray(allItems));
    }
    setIsShuffled(!isShuffled);
  };

  const playAudio = useCallback(
    (url: string): Promise<void> => {
      return new Promise((resolve, reject) => {
        if (audioRef.current) {
          audioRef.current.pause();
        }

        const audio = new Audio(url);
        audio.playbackRate = speed;
        audioRef.current = audio;

        audio.onended = () => resolve();
        audio.onerror = () => reject(new Error('Audio failed to load'));

        audio.play().catch(reject);
      });
    },
    [speed]
  );

  const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

  const waitWhilePaused = async () => {
    while (isPausedRef.current && !shouldStopRef.current) {
      await sleep(100);
    }
  };

  const playSequence = useCallback(async () => {
    shouldStopRef.current = false;
    setIsPlaying(true);
    setIsPaused(false);

    for (let i = 0; i < items.length; i++) {
      if (shouldStopRef.current) break;

      await waitWhilePaused();
      if (shouldStopRef.current) break;

      const item = items[i];
      if (!item.audioUrl) continue;

      setCurrentIndex(i);

      for (let rep = 1; rep <= repeatCount; rep++) {
        if (shouldStopRef.current) break;

        await waitWhilePaused();
        if (shouldStopRef.current) break;

        setCurrentRepeat(rep);

        try {
          await playAudio(item.audioUrl);
          setPracticedIds((prev) => new Set(prev).add(item.id));
        } catch {
          // Continue to next item on error
        }

        // Brief pause between repeats
        if (rep < repeatCount) {
          await sleep(500);
        }
      }

      // Pause between items
      if (i < items.length - 1) {
        await sleep(800);
      }
    }

    setIsPlaying(false);
    setIsPaused(false);
    setCurrentIndex(null);
    setCurrentRepeat(1);
  }, [items, repeatCount, playAudio]);

  const handlePlayAll = () => {
    if (isPlaying && !isPaused) {
      // Pause
      setIsPaused(true);
      if (audioRef.current) {
        audioRef.current.pause();
      }
    } else if (isPaused) {
      // Resume
      setIsPaused(false);
      if (audioRef.current) {
        audioRef.current.play();
      }
    } else {
      // Start fresh
      playSequence();
    }
  };

  const handleStop = () => {
    shouldStopRef.current = true;
    setIsPlaying(false);
    setIsPaused(false);
    setCurrentIndex(null);
    setCurrentRepeat(1);
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
  };

  const handlePlaySingle = async (item: PracticeItem, index: number) => {
    if (!item.audioUrl) return;

    // Stop any current playback
    handleStop();

    setCurrentIndex(index);
    setIsPlaying(true);

    for (let rep = 1; rep <= repeatCount; rep++) {
      setCurrentRepeat(rep);
      try {
        await playAudio(item.audioUrl);
        setPracticedIds((prev) => new Set(prev).add(item.id));
      } catch {
        break;
      }
      if (rep < repeatCount) {
        await sleep(500);
      }
    }

    setIsPlaying(false);
    setCurrentIndex(null);
    setCurrentRepeat(1);
  };

  const resetProgress = () => {
    setPracticedIds(new Set());
  };

  const progressPercentage =
    items.length > 0 ? Math.round((practicedIds.size / items.length) * 100) : 0;

  if (allItems.length === 0) {
    return (
      <div className="rounded-xl border border-border bg-bg-card p-8 text-center">
        <svg
          className="mx-auto mb-4 h-12 w-12 text-text-secondary"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
          />
        </svg>
        <h3 className="mb-2 text-lg font-semibold text-text-primary">No Audio Available</h3>
        <p className="text-text-secondary">
          There are no vocabulary or expressions with audio in this lesson.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with Controls */}
      <div className="rounded-xl border border-border bg-bg-card p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-text-primary">Practice Pronunciation</h3>
            <p className="text-sm text-text-secondary">
              {items.length} items to practice ({vocabulary.filter((v) => v.audioUrl).length}{' '}
              vocabulary, {expressions.filter((e) => e.audioUrl).length} expressions)
            </p>
          </div>

          <div className="flex items-center gap-2">
            {/* Settings Button */}
            <button
              onClick={() => setShowSettings(!showSettings)}
              className={`flex h-10 w-10 items-center justify-center rounded-lg border transition-colors ${
                showSettings
                  ? 'border-accent-primary bg-accent-primary/10 text-accent-primary'
                  : 'border-border text-text-secondary hover:border-accent-primary/50 hover:text-text-primary'
              }`}
              title="Settings"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                />
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
            </button>

            {/* Shuffle Button */}
            <button
              onClick={toggleShuffle}
              disabled={isPlaying}
              className={`flex h-10 w-10 items-center justify-center rounded-lg border transition-colors ${
                isShuffled
                  ? 'border-accent-primary bg-accent-primary/10 text-accent-primary'
                  : 'border-border text-text-secondary hover:border-accent-primary/50 hover:text-text-primary'
              } disabled:opacity-50`}
              title={isShuffled ? 'Unshuffle' : 'Shuffle'}
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"
                />
              </svg>
            </button>

            {/* Stop Button (when playing) */}
            {isPlaying && (
              <button
                onClick={handleStop}
                className="flex h-10 w-10 items-center justify-center rounded-lg border border-error/50 bg-error/10 text-error transition-colors hover:bg-error/20"
                title="Stop"
              >
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                  <rect x="6" y="6" width="12" height="12" rx="1" />
                </svg>
              </button>
            )}

            {/* Play All Button */}
            <button
              onClick={handlePlayAll}
              className={`flex h-10 items-center gap-2 rounded-lg px-4 font-medium transition-colors ${
                isPlaying && !isPaused
                  ? 'bg-accent-primary/20 text-accent-primary'
                  : 'bg-accent-primary text-white hover:bg-accent-primary/90'
              }`}
            >
              {isPlaying && !isPaused ? (
                <>
                  <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
                  </svg>
                  Pause
                </>
              ) : isPaused ? (
                <>
                  <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                  Resume
                </>
              ) : (
                <>
                  <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                  Play All
                </>
              )}
            </button>
          </div>
        </div>

        {/* Settings Panel */}
        {showSettings && (
          <div className="mb-4 rounded-lg border border-border bg-bg-dark p-4">
            <div className="grid gap-4 sm:grid-cols-2">
              {/* Speed Control */}
              <div>
                <label className="mb-2 block text-sm font-medium text-text-secondary">
                  Playback Speed
                </label>
                <div className="flex gap-1">
                  {SPEEDS.map((s) => (
                    <button
                      key={s}
                      onClick={() => setSpeed(s)}
                      className={`rounded-lg px-3 py-1.5 font-mono text-sm transition-colors ${
                        speed === s
                          ? 'bg-accent-primary text-white'
                          : 'bg-bg-card text-text-secondary hover:text-text-primary'
                      }`}
                    >
                      {s}x
                    </button>
                  ))}
                </div>
              </div>

              {/* Repeat Control */}
              <div>
                <label className="mb-2 block text-sm font-medium text-text-secondary">
                  Repeat Each Word
                </label>
                <div className="flex gap-1">
                  {REPEAT_OPTIONS.map((r) => (
                    <button
                      key={r}
                      onClick={() => setRepeatCount(r)}
                      className={`rounded-lg px-3 py-1.5 text-sm transition-colors ${
                        repeatCount === r
                          ? 'bg-accent-primary text-white'
                          : 'bg-bg-card text-text-secondary hover:text-text-primary'
                      }`}
                    >
                      {r}x
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Progress Bar */}
        <div>
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">
              {practicedIds.size} of {items.length} practiced
            </span>
            <button
              onClick={resetProgress}
              className="text-sm text-text-secondary transition-colors hover:text-accent-primary"
            >
              Reset
            </button>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-bg-dark">
            <div
              className="h-full rounded-full bg-accent-primary transition-all duration-300"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>
      </div>

      {/* Items List */}
      <div className="space-y-2">
        {items.map((item, index) => (
          <div
            key={item.id}
            className={`flex items-center gap-4 rounded-xl border p-4 transition-all ${
              currentIndex === index
                ? 'border-accent-primary bg-accent-primary/5'
                : practicedIds.has(item.id)
                  ? 'border-success/30 bg-success/5'
                  : 'border-border bg-bg-card hover:border-accent-primary/30'
            }`}
          >
            {/* Index */}
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-bg-dark font-mono text-sm text-text-secondary">
              {index + 1}
            </span>

            {/* Content */}
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <span className="font-medium text-text-primary">{item.text}</span>
                <span
                  className={`rounded px-1.5 py-0.5 text-xs ${
                    item.type === 'vocabulary'
                      ? 'bg-blue-500/10 text-blue-400'
                      : 'bg-purple-500/10 text-purple-400'
                  }`}
                >
                  {item.type}
                </span>
                {practicedIds.has(item.id) && (
                  <svg className="h-4 w-4 text-success" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fillRule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clipRule="evenodd"
                    />
                  </svg>
                )}
              </div>
              <p className="truncate text-sm text-text-secondary">{item.definition}</p>
            </div>

            {/* Play Button */}
            <button
              onClick={() => handlePlaySingle(item, index)}
              disabled={isPlaying && currentIndex !== index}
              className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg transition-colors ${
                currentIndex === index
                  ? 'bg-accent-primary text-white'
                  : 'text-text-secondary hover:bg-bg-dark hover:text-accent-primary disabled:opacity-50'
              }`}
              title="Play"
            >
              {currentIndex === index ? (
                <div className="flex items-center gap-0.5">
                  <span className="inline-block h-3 w-1 animate-pulse rounded-full bg-current" />
                  <span
                    className="inline-block h-3 w-1 animate-pulse rounded-full bg-current"
                    style={{ animationDelay: '150ms' }}
                  />
                  <span
                    className="inline-block h-3 w-1 animate-pulse rounded-full bg-current"
                    style={{ animationDelay: '300ms' }}
                  />
                </div>
              ) : (
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z" />
                </svg>
              )}
            </button>

            {/* Repeat indicator when playing */}
            {currentIndex === index && repeatCount > 1 && (
              <span className="shrink-0 font-mono text-sm text-accent-primary">
                {currentRepeat}/{repeatCount}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
