import { useState, useRef, useEffect, useCallback } from 'react';
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis';
import { useMediaRecorder } from '../../hooks/useMediaRecorder';
import { useTranscribePronunciation } from '../../hooks/usePronunciation';
import type { Vocabulary, Expression } from '../../types/lesson';
import type { TranscriptionResult } from '../../api/pronunciation';

type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;
type PracticeItem = {
  id: string;
  text: string;
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
  const allItems: PracticeItem[] = [
    ...vocabulary.map((v) => ({
      id: v.id,
      text: v.term,
      type: 'vocabulary' as const,
      definition: v.definition,
    })),
    ...expressions.map((e) => ({
      id: e.id,
      text: e.phrase,
      type: 'expression' as const,
      definition: e.meaning,
    })),
  ];

  const [items, setItems] = useState<PracticeItem[]>(allItems);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [currentIndex, setCurrentIndex] = useState<number | null>(null);
  const [currentRepeat, setCurrentRepeat] = useState(1);
  const [repeatCount, setRepeatCount] = useState(2);
  const [isShuffled, setIsShuffled] = useState(false);
  const [practicedIds, setPracticedIds] = useState<Set<string>>(new Set());
  const [showSettings, setShowSettings] = useState(false);
  const [recordingItemId, setRecordingItemId] = useState<string | null>(null);
  const [pronunciationResults, setPronunciationResults] = useState<Map<string, TranscriptionResult>>(
    new Map()
  );
  const [pronunciationScores, setPronunciationScores] = useState<Map<string, number>>(new Map());

  const shouldStopRef = useRef(false);
  const isPausedRef = useRef(false);

  const tts = useSpeechSynthesis();
  const recorder = useMediaRecorder();
  const transcribeMutation = useTranscribePronunciation();

  // Keep isPausedRef in sync
  useEffect(() => {
    isPausedRef.current = isPaused;
  }, [isPaused]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      speechSynthesis.cancel();
    };
  }, []);

  // Auto-submit recording when stopped
  useEffect(() => {
    if (recorder.state === 'stopped' && recorder.audioBlob && recordingItemId) {
      const item = items.find((i) => i.id === recordingItemId);
      if (item) {
        transcribeMutation.mutate(
          {
            expectedText: item.text,
            audioBlob: recorder.audioBlob,
          },
          {
            onSuccess: (result) => {
              setPronunciationResults((prev) => new Map(prev).set(recordingItemId, result));
              // Update best score
              setPronunciationScores((prev) => {
                const newScores = new Map(prev);
                const currentBest = newScores.get(recordingItemId) ?? 0;
                if (result.similarity > currentBest) {
                  newScores.set(recordingItemId, result.similarity);
                }
                return newScores;
              });
              recorder.reset();
            },
            onError: () => {
              recorder.reset();
            },
          }
        );
      }
      setRecordingItemId(null);
    }
  }, [recorder.state, recorder.audioBlob, recordingItemId, items, transcribeMutation, recorder]);

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

  const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

  const waitWhilePaused = async () => {
    while (isPausedRef.current && !shouldStopRef.current) {
      await sleep(100);
    }
  };

  const speakAndWait = useCallback(
    (text: string): Promise<void> => {
      return new Promise((resolve, reject) => {
        speechSynthesis.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'en-US';
        utterance.rate = tts.rate;

        utterance.onend = () => resolve();
        utterance.onerror = (e) => {
          if (e.error === 'canceled') {
            reject(new Error('canceled'));
          } else {
            reject(new Error(e.error));
          }
        };

        speechSynthesis.speak(utterance);
      });
    },
    [tts.rate],
  );

  const playSequence = useCallback(async () => {
    shouldStopRef.current = false;
    setIsPlaying(true);
    setIsPaused(false);

    for (let i = 0; i < items.length; i++) {
      if (shouldStopRef.current) break;

      await waitWhilePaused();
      if (shouldStopRef.current) break;

      const item = items[i];
      setCurrentIndex(i);

      for (let rep = 1; rep <= repeatCount; rep++) {
        if (shouldStopRef.current) break;

        await waitWhilePaused();
        if (shouldStopRef.current) break;

        setCurrentRepeat(rep);

        try {
          await speakAndWait(item.text);
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
  }, [items, repeatCount, speakAndWait]);

  const handlePlayAll = () => {
    if (isPlaying && !isPaused) {
      // Pause
      setIsPaused(true);
      speechSynthesis.pause();
    } else if (isPaused) {
      // Resume
      setIsPaused(false);
      speechSynthesis.resume();
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
    speechSynthesis.cancel();
  };

  const handlePlaySingle = async (item: PracticeItem, index: number) => {
    // Stop any current playback
    handleStop();

    setCurrentIndex(index);
    setIsPlaying(true);
    shouldStopRef.current = false;

    for (let rep = 1; rep <= repeatCount; rep++) {
      if (shouldStopRef.current) break;
      setCurrentRepeat(rep);
      try {
        await speakAndWait(item.text);
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

  const handleMicClick = (itemId: string) => {
    if (recorder.state === 'recording' && recordingItemId === itemId) {
      // Stop recording for this item
      recorder.stopRecording();
    } else if (recorder.state === 'idle') {
      // Start recording for this item
      setRecordingItemId(itemId);
      setPronunciationResults((prev) => {
        const newMap = new Map(prev);
        newMap.delete(itemId);
        return newMap;
      });
      recorder.startRecording();
    }
  };

  const handleTryAgain = (itemId: string) => {
    setPronunciationResults((prev) => {
      const newMap = new Map(prev);
      newMap.delete(itemId);
      return newMap;
    });
  };

  const progressPercentage =
    items.length > 0 ? Math.round((practicedIds.size / items.length) * 100) : 0;

  if (allItems.length === 0) {
    return (
      <div className="rounded-xl border border-edge-default bg-bg-card p-8 text-center">
        <svg
          className="mx-auto mb-4 h-12 w-12 text-content-secondary"
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
        <h3 className="mb-2 text-lg font-semibold text-content-primary">Nothing to Practice</h3>
        <p className="text-content-secondary">
          There are no vocabulary or expressions in this lesson yet.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with Controls */}
      <div className="rounded-xl border border-edge-default bg-bg-card p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-content-primary">Practice Pronunciation</h3>
            <p className="text-sm text-content-secondary">
              {items.length} items to practice ({vocabulary.length} vocabulary,{' '}
              {expressions.length} expressions)
            </p>
          </div>

          <div className="flex items-center gap-2">
            {/* Settings Button */}
            <button
              onClick={() => setShowSettings(!showSettings)}
              className={`flex h-10 w-10 items-center justify-center rounded-lg border transition-colors ${
                showSettings
                  ? 'border-brand bg-brand-muted text-brand'
                  : 'border-edge-default text-content-secondary hover:border-brand/50 hover:text-content-primary'
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
                  ? 'border-brand bg-brand-muted text-brand'
                  : 'border-edge-default text-content-secondary hover:border-brand/50 hover:text-content-primary'
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
                  ? 'bg-brand-muted text-brand'
                  : 'bg-brand text-white hover:bg-brand-hover'
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
          <div className="mb-4 rounded-lg border border-edge-default bg-bg-inset p-4">
            <div className="grid gap-4 sm:grid-cols-2">
              {/* Speed Control */}
              <div>
                <label className="mb-2 block text-sm font-medium text-content-secondary">
                  Playback Speed
                </label>
                <div className="flex gap-1">
                  {SPEEDS.map((s) => (
                    <button
                      key={s}
                      onClick={() => tts.setRate(s)}
                      className={`rounded-lg px-3 py-1.5 font-mono text-sm transition-colors ${
                        tts.rate === s
                          ? 'bg-brand text-white'
                          : 'bg-bg-card text-content-secondary hover:text-content-primary'
                      }`}
                    >
                      {s}x
                    </button>
                  ))}
                </div>
              </div>

              {/* Repeat Control */}
              <div>
                <label className="mb-2 block text-sm font-medium text-content-secondary">
                  Repeat Each Word
                </label>
                <div className="flex gap-1">
                  {REPEAT_OPTIONS.map((r) => (
                    <button
                      key={r}
                      onClick={() => setRepeatCount(r)}
                      className={`rounded-lg px-3 py-1.5 text-sm transition-colors ${
                        repeatCount === r
                          ? 'bg-brand text-white'
                          : 'bg-bg-card text-content-secondary hover:text-content-primary'
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
            <span className="text-sm text-content-secondary">
              {practicedIds.size} of {items.length} practiced
            </span>
            <button
              onClick={resetProgress}
              className="text-sm text-content-secondary transition-colors hover:text-brand"
            >
              Reset
            </button>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-bg-inset">
            <div
              className="h-full rounded-full bg-brand transition-all duration-300"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>
      </div>

      {/* Items List */}
      <div className="space-y-2">
        {items.map((item, index) => {
          const isRecording = recorder.state === 'recording' && recordingItemId === item.id;
          const isProcessing = transcribeMutation.isPending && recordingItemId === item.id;
          const result = pronunciationResults.get(item.id);
          const bestScore = pronunciationScores.get(item.id);
          const isRecordingAny = recorder.state === 'recording';

          return (
            <div
              key={item.id}
              className={`rounded-xl border p-4 transition-all ${
                currentIndex === index
                  ? 'border-brand bg-brand-muted'
                  : practicedIds.has(item.id)
                    ? 'border-success/30 bg-success/5'
                    : 'border-edge-default bg-bg-card hover:border-brand/30'
              }`}
            >
              <div className="flex items-center gap-4">
                {/* Index */}
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-bg-inset font-mono text-sm text-content-secondary">
                  {index + 1}
                </span>

                {/* Content */}
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-content-primary">{item.text}</span>
                    <span
                      className={`rounded px-1.5 py-0.5 text-xs ${
                        item.type === 'vocabulary'
                          ? 'bg-info-muted text-info'
                          : 'bg-brand-muted text-brand'
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
                    {bestScore !== undefined && (
                      <span className="font-mono text-xs text-success">
                        {Math.round(bestScore * 100)}%
                      </span>
                    )}
                  </div>
                  <p className="truncate text-sm text-content-secondary">{item.definition}</p>
                </div>

                {/* Microphone Button */}
                <button
                  onClick={() => handleMicClick(item.id)}
                  disabled={isPlaying || isProcessing || (isRecordingAny && !isRecording)}
                  className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg transition-colors ${
                    isRecording
                      ? 'animate-pulse bg-error text-white'
                      : isProcessing
                        ? 'bg-bg-inset text-content-secondary'
                        : 'text-content-secondary hover:bg-bg-inset hover:text-brand disabled:opacity-50'
                  }`}
                  title={isRecording ? 'Stop recording' : 'Record pronunciation'}
                >
                  {isProcessing ? (
                    <svg
                      className="h-5 w-5 animate-spin"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                      />
                    </svg>
                  ) : (
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
                      />
                    </svg>
                  )}
                </button>

                {/* Play Button */}
                <button
                  onClick={() => handlePlaySingle(item, index)}
                  disabled={(isPlaying && currentIndex !== index) || isRecordingAny}
                  className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg transition-colors ${
                    currentIndex === index
                      ? 'bg-brand text-white'
                      : 'text-content-secondary hover:bg-bg-inset hover:text-brand disabled:opacity-50'
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

                {/* Recording duration or repeat indicator */}
                {isRecording ? (
                  <span className="shrink-0 font-mono text-sm text-error">
                    0:{recorder.duration.toString().padStart(2, '0')}
                  </span>
                ) : currentIndex === index && repeatCount > 1 ? (
                  <span className="shrink-0 font-mono text-sm text-brand">
                    {currentRepeat}/{repeatCount}
                  </span>
                ) : (
                  <div className="w-12" />
                )}
              </div>

              {/* Pronunciation Result */}
              {result && (
                <div
                  className={`mt-3 rounded-lg border p-3 ${
                    result.passed
                      ? 'border-success/30 bg-success/10'
                      : 'border-error/30 bg-error/10'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1">
                      <div className="mb-1 flex items-center gap-2">
                        <span
                          className={`inline-flex items-center gap-1 rounded px-2 py-0.5 font-mono text-sm font-medium ${
                            result.passed
                              ? 'bg-success/20 text-success'
                              : 'bg-error/20 text-error'
                          }`}
                        >
                          {Math.round(result.similarity * 100)}% match
                          {result.passed && (
                            <svg className="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 20 20">
                              <path
                                fillRule="evenodd"
                                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                clipRule="evenodd"
                              />
                            </svg>
                          )}
                        </span>
                      </div>
                      <p className="text-sm text-content-secondary">
                        You said: <span className="text-content-primary">{result.transcription}</span>
                      </p>
                      {!result.passed && (
                        <p className="text-sm text-content-secondary">
                          Expected: <span className="text-content-primary">{result.expectedText}</span>
                          {result.expectedIpa && (
                            <span className="ml-2 font-mono text-xs text-content-secondary">
                              {result.expectedIpa}
                            </span>
                          )}
                        </p>
                      )}
                      {result.suggestions && result.suggestions.length > 0 && (
                        <ul className="mt-2 space-y-1">
                          {result.suggestions.map((suggestion, i) => (
                            <li key={i} className="flex items-start gap-1.5 text-sm text-content-secondary">
                              <span className="mt-0.5 shrink-0 text-brand">&#x2022;</span>
                              {suggestion.tip}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                    {!result.passed && (
                      <button
                        onClick={() => handleTryAgain(item.id)}
                        className="shrink-0 rounded px-3 py-1 text-sm font-medium text-error transition-colors hover:bg-error/20"
                      >
                        Try Again
                      </button>
                    )}
                  </div>
                </div>
              )}

              {/* Error Display */}
              {recorder.error && recordingItemId === item.id && (
                <div className="mt-3 rounded-lg border border-error/30 bg-error/10 p-3">
                  <p className="text-sm text-error">{recorder.error}</p>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
