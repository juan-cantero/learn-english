import { useState, useCallback, useEffect, useRef } from 'react';
import { useShadowingScenes } from '../../hooks/useLesson';
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis';
import { useSpeechRecognition, calculateSimilarity } from '../../hooks/useSpeechRecognition';
import type { ShadowingScene, ShadowingDialogueLine } from '../../types/lesson';

interface ShadowingPracticeProps {
  showSlug: string;
  episodeSlug: string;
}

type ViewState = 'loading' | 'scene-selection' | 'character-selection' | 'practice';

interface LineResult {
  lineIndex: number;
  similarity: number;
  transcript: string;
}

const FAKE_STEPS = [
  { label: 'Analyzing episode script...', delay: 2000 },
  { label: 'Identifying dialogue scenes...', delay: 3000 },
  { label: 'Selecting best scenes for practice...', delay: 2500 },
  { label: 'Preparing shadowing exercises...', delay: 1500 },
];

export function ShadowingPractice({ showSlug, episodeSlug }: ShadowingPracticeProps) {
  const [viewState, setViewState] = useState<ViewState>('loading');
  const [selectedScene, setSelectedScene] = useState<ShadowingScene | null>(null);
  const [selectedCharacter, setSelectedCharacter] = useState<string | null>(null);
  const [currentLineIndex, setCurrentLineIndex] = useState(0);
  const [lineResults, setLineResults] = useState<Map<number, LineResult>>(new Map());
  const [waitingForUser, setWaitingForUser] = useState(false);
  const [loadingStep, setLoadingStep] = useState(0);
  const [animationDone, setAnimationDone] = useState(false);
  const hasAutoPlayedRef = useRef(false);
  const hasInitRef = useRef(false);

  // Auto-fetch on mount — backend returns cached scenes instantly if they exist
  const { data: scenes, isLoading, error } = useShadowingScenes(showSlug, episodeSlug);
  const {
    transcript,
    isListening,
    isSupported: recognitionSupported,
    start: startRecognition,
    stop: stopRecognition,
    reset: resetRecognition,
    error: recognitionError,
  } = useSpeechRecognition();
  const { state: speechState, speak, stop: stopSpeech, supported: speechSupported } = useSpeechSynthesis({
    rate: 1,
    onEnd: handleSpeechEnd,
  });

  const currentLine: ShadowingDialogueLine | undefined = selectedScene?.lines[currentLineIndex];
  const isUserLine = currentLine?.character === selectedCharacter;
  const totalLines = selectedScene?.lines.length ?? 0;
  const completedLines = lineResults.size;
  const progressPercentage = totalLines > 0 ? (completedLines / totalLines) * 100 : 0;

  function handleSpeechEnd() {
    // When TTS finishes reading an NPC line, register as completed and move to next
    if (!isUserLine) {
      setLineResults((prev) => {
        const next = new Map(prev);
        if (!next.has(currentLineIndex)) {
          next.set(currentLineIndex, { lineIndex: currentLineIndex, similarity: 100, transcript: '' });
        }
        return next;
      });
      if (currentLineIndex < totalLines - 1) {
        setTimeout(() => {
          setCurrentLineIndex((prev) => prev + 1);
          hasAutoPlayedRef.current = false;
        }, 800);
      } else {
        // Last line was NPC - show completion
        setTimeout(() => {
          setCurrentLineIndex(totalLines);
        }, 800);
      }
    }
  }

  const runFakeLoading = useCallback(async () => {
    setLoadingStep(0);
    for (let i = 0; i < FAKE_STEPS.length; i++) {
      setLoadingStep(i);
      await new Promise((r) => setTimeout(r, FAKE_STEPS[i].delay));
    }
    setAnimationDone(true);
  }, []);

  // On mount: if data is in TanStack cache, skip animation. Otherwise, animate.
  useEffect(() => {
    if (hasInitRef.current) return;
    hasInitRef.current = true;

    if (!isLoading && scenes && scenes.length > 0) {
      // Data returned from cache instantly — skip animation (e.g. tab switch)
      setViewState('scene-selection');
      return;
    }

    // No cached data — start fake loading while backend fetches/generates
    runFakeLoading();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // When both animation done AND data ready → show scenes
  useEffect(() => {
    if (viewState !== 'loading') return;
    if (animationDone && scenes && scenes.length > 0) {
      setViewState('scene-selection');
    }
  }, [animationDone, scenes, viewState]);

  const handleSceneSelect = useCallback((scene: ShadowingScene) => {
    setSelectedScene(scene);
    setViewState('character-selection');
  }, []);

  const handleCharacterSelect = useCallback((character: string) => {
    setSelectedCharacter(character);
    setCurrentLineIndex(0);
    setLineResults(new Map());
    setWaitingForUser(false);
    hasAutoPlayedRef.current = false;
    setViewState('practice');
  }, []);

  const handleRecordClick = useCallback(() => {
    if (isListening) {
      stopRecognition();
    } else {
      resetRecognition();
      startRecognition();
    }
  }, [isListening, startRecognition, stopRecognition, resetRecognition]);

  const handleNextLine = useCallback(() => {
    if (currentLineIndex < totalLines - 1) {
      setCurrentLineIndex((prev) => prev + 1);
      setWaitingForUser(false);
      hasAutoPlayedRef.current = false;
      resetRecognition();
    } else {
      // Last line completed
      setCurrentLineIndex(totalLines);
    }
  }, [currentLineIndex, totalLines, resetRecognition]);

  const handleRepeatLine = useCallback(() => {
    setLineResults((prev) => {
      const next = new Map(prev);
      next.delete(currentLineIndex);
      return next;
    });
    setWaitingForUser(false);
    resetRecognition();
  }, [currentLineIndex, resetRecognition]);

  const handleCheckSimilarity = useCallback(() => {
    if (!currentLine || !transcript) return;

    const similarity = calculateSimilarity(currentLine.text, transcript);
    const result: LineResult = {
      lineIndex: currentLineIndex,
      similarity,
      transcript,
    };

    setLineResults((prev) => new Map(prev).set(currentLineIndex, result));
    setWaitingForUser(true);
    stopRecognition();
  }, [currentLine, transcript, currentLineIndex, stopRecognition]);

  const handleBackToScenes = useCallback(() => {
    setSelectedScene(null);
    setSelectedCharacter(null);
    setCurrentLineIndex(0);
    setLineResults(new Map());
    setWaitingForUser(false);
    stopSpeech();
    resetRecognition();
    setViewState('scene-selection');
  }, [stopSpeech, resetRecognition]);

  // Auto-play NPC lines when they become current
  useEffect(() => {
    if (
      viewState === 'practice' &&
      currentLine &&
      !isUserLine &&
      !hasAutoPlayedRef.current &&
      speechSupported
    ) {
      hasAutoPlayedRef.current = true;
      // Small delay for better UX
      setTimeout(() => {
        speak(currentLine.text);
      }, 500);
    }
  }, [viewState, currentLine, isUserLine, speechSupported, speak]);

  // Check if we should auto-check similarity when user stops speaking
  useEffect(() => {
    if (
      !isListening &&
      transcript &&
      isUserLine &&
      !waitingForUser &&
      !lineResults.has(currentLineIndex)
    ) {
      // User stopped speaking, auto-check
      handleCheckSimilarity();
    }
  }, [isListening, transcript, isUserLine, waitingForUser, lineResults, currentLineIndex, handleCheckSimilarity]);

  const currentResult = lineResults.get(currentLineIndex);

  // State 1: Loading (fake animation + real fetch in parallel)
  if (viewState === 'loading') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-6">
        <div className="w-12 h-12 border-4 border-brand border-t-transparent rounded-full animate-spin"></div>
        <div className="text-center">
          <p className="text-content-primary font-medium mb-2">
            {FAKE_STEPS[loadingStep]?.label ?? 'Preparing...'}
          </p>
          <p className="text-content-secondary text-sm">
            This may take a moment while AI analyzes the episode
          </p>
        </div>
        <div className="flex gap-2">
          {FAKE_STEPS.map((_, i) => (
            <div
              key={i}
              className={`w-2 h-2 rounded-full transition-colors ${
                i <= loadingStep ? 'bg-brand' : 'bg-bg-card'
              }`}
            />
          ))}
        </div>
        {error && animationDone && (
          <div className="p-4 bg-error/10 border border-error rounded-lg text-center mt-4">
            <p className="text-error text-sm">
              Failed to generate shadowing scenes. Please try again later.
            </p>
          </div>
        )}
      </div>
    );
  }

  // State 2: Scene Selection
  if (viewState === 'scene-selection' && scenes) {
    return (
      <div className="space-y-6">
        <div className="text-center">
          <h3 className="text-xl font-semibold text-content-primary mb-2">
            Choose a Scene
          </h3>
          <p className="text-content-secondary">
            Select a scene to practice shadowing dialogue
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {scenes.map((scene) => (
            <button
              key={scene.id}
              onClick={() => handleSceneSelect(scene)}
              className="text-left p-4 bg-bg-card border border-edge-default rounded-lg hover:border-brand hover:bg-brand/5 transition-colors"
            >
              <h4 className="text-lg font-medium text-content-primary mb-2">
                {scene.title}
              </h4>
              <div className="flex items-center gap-2 text-sm text-content-secondary">
                <span>{scene.lines.length} lines</span>
                <span>•</span>
                <span>{scene.characters.join(', ')}</span>
              </div>
            </button>
          ))}
        </div>
      </div>
    );
  }

  // State 2.5: Character Selection
  if (viewState === 'character-selection' && selectedScene) {
    return (
      <div className="space-y-6">
        <div className="text-center">
          <h3 className="text-xl font-semibold text-content-primary mb-2">
            {selectedScene.title}
          </h3>
          <p className="text-content-secondary">
            Choose which character you want to play
          </p>
        </div>
        <div className="flex flex-wrap justify-center gap-3">
          {selectedScene.characters.map((character) => (
            <button
              key={character}
              onClick={() => handleCharacterSelect(character)}
              className="px-6 py-3 bg-bg-card border border-edge-default rounded-full text-content-primary font-medium hover:border-brand hover:bg-brand/5 transition-colors"
            >
              {character}
            </button>
          ))}
        </div>
        <div className="text-center">
          <button
            onClick={() => setViewState('scene-selection')}
            className="text-sm text-content-secondary hover:text-content-primary transition-colors"
          >
            Back to scenes
          </button>
        </div>
      </div>
    );
  }

  // State 3: Practice Mode
  if (viewState === 'practice' && selectedScene && selectedCharacter) {
    const hasResult = currentResult !== undefined;

    return (
      <div className="space-y-4">
        {/* Progress Bar */}
        <div className="sticky top-0 bg-bg-dark z-10 pb-4">
          <div className="flex items-center justify-between mb-2 text-sm">
            <span className="text-content-secondary">
              Line {currentLineIndex + 1} of {totalLines}
            </span>
            <span className="text-content-secondary">
              {Math.round(progressPercentage)}% complete
            </span>
          </div>
          <div className="h-1.5 bg-bg-card rounded-full overflow-hidden">
            <div
              className="h-full bg-brand transition-all duration-300"
              style={{ width: `${progressPercentage}%` }}
            ></div>
          </div>
        </div>

        {/* Scene Header */}
        <div className="flex items-center justify-between pb-2 border-b border-edge-default">
          <div>
            <h3 className="text-lg font-semibold text-content-primary">
              {selectedScene.title}
            </h3>
            <p className="text-sm text-content-secondary">
              You are playing: <span className="text-brand font-medium">{selectedCharacter}</span>
            </p>
          </div>
          <button
            onClick={handleBackToScenes}
            className="text-sm text-content-secondary hover:text-content-primary transition-colors"
          >
            Change scene
          </button>
        </div>

        {/* Dialogue Display */}
        <div className="space-y-4 py-4">
          {selectedScene.lines.slice(Math.max(0, currentLineIndex - 2), currentLineIndex + 3).map((line, idx) => {
            const lineIdx = Math.max(0, currentLineIndex - 2) + idx;
            const isCurrent = lineIdx === currentLineIndex;
            const isPast = lineIdx < currentLineIndex;
            const isCurrentUserLine = line.character === selectedCharacter;
            const result = lineResults.get(lineIdx);

            return (
              <div
                key={lineIdx}
                className={`flex ${isCurrentUserLine ? 'justify-end' : 'justify-start'} ${
                  !isCurrent && !isPast ? 'opacity-40' : ''
                } ${isPast ? 'opacity-60' : ''}`}
              >
                <div
                  className={`max-w-[80%] rounded-lg p-4 ${
                    isCurrentUserLine
                      ? `${
                          result
                            ? result.similarity >= 70
                              ? 'bg-success/10 border border-success'
                              : 'bg-error/10 border border-error'
                            : isCurrent && !hasResult
                            ? 'border-2 border-brand border-dashed bg-brand/5'
                            : 'bg-brand/10 border border-brand/50'
                        }`
                      : 'bg-bg-card border border-edge-default'
                  } ${isCurrent ? 'ring-2 ring-brand/30' : ''}`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-medium text-content-secondary">
                      {line.character}
                    </span>
                    {line.startTime && (
                      <span className="text-xs text-content-secondary/70">{line.startTime}</span>
                    )}
                  </div>
                  <p className="text-content-primary">{line.text}</p>
                  {result && (
                    <div className="mt-2 pt-2 border-t border-edge-default">
                      <p className="text-xs text-content-secondary mb-1">You said:</p>
                      <p className="text-sm text-content-primary italic mb-2">"{result.transcript}"</p>
                      <div className="flex items-center gap-2">
                        <span
                          className={`text-sm font-medium ${
                            result.similarity >= 70 ? 'text-success' : 'text-error'
                          }`}
                        >
                          {result.similarity}% match
                        </span>
                        {result.similarity >= 70 ? (
                          <span className="text-xs text-success">Great job!</span>
                        ) : (
                          <span className="text-xs text-error">Try again</span>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Controls */}
        <div className="sticky bottom-0 bg-bg-dark pt-4 border-t border-edge-default">
          {currentLineIndex >= totalLines ? (
            <div className="text-center py-8">
              <h3 className="text-xl font-semibold text-content-primary mb-2">
                Practice Complete!
              </h3>
              <p className="text-content-secondary mb-4">
                You've completed all lines in this scene
              </p>
              <button
                onClick={handleBackToScenes}
                className="px-6 py-3 bg-brand text-white rounded-lg font-medium hover:bg-brand/90 transition-colors"
              >
                Choose Another Scene
              </button>
            </div>
          ) : isUserLine ? (
            <div className="space-y-3">
              {!recognitionSupported ? (
                <div className="p-4 bg-error/10 border border-error rounded-lg text-center">
                  <p className="text-error text-sm">
                    Speech recognition is not supported in your browser
                  </p>
                </div>
              ) : recognitionError ? (
                <div className="p-4 bg-error/10 border border-error rounded-lg text-center">
                  <p className="text-error text-sm">Error: {recognitionError}</p>
                </div>
              ) : waitingForUser ? (
                <div className="flex gap-3">
                  <button
                    onClick={handleNextLine}
                    className="flex-1 px-6 py-3 bg-brand text-white rounded-lg font-medium hover:bg-brand/90 transition-colors"
                  >
                    Next Line
                  </button>
                  <button
                    onClick={handleRepeatLine}
                    className="px-6 py-3 bg-bg-card border border-edge-default text-content-primary rounded-lg font-medium hover:border-brand transition-colors"
                  >
                    Try Again
                  </button>
                </div>
              ) : (
                <div className="flex flex-col items-center gap-3">
                  <button
                    onClick={handleRecordClick}
                    className={`w-16 h-16 rounded-full flex items-center justify-center transition-all ${
                      isListening
                        ? 'bg-error text-white animate-pulse'
                        : 'bg-brand text-white hover:bg-brand/90'
                    }`}
                    aria-label={isListening ? 'Stop recording' : 'Start recording'}
                  >
                    {isListening ? (
                      <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                        <rect x="8" y="8" width="8" height="8" rx="1" />
                      </svg>
                    ) : (
                      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4M12 15a3 3 0 003-3V6a3 3 0 00-6 0v6a3 3 0 003 3z" />
                      </svg>
                    )}
                  </button>
                  <p className="text-sm text-content-secondary">
                    {isListening ? 'Listening...' : 'Tap to speak your line'}
                  </p>
                  {transcript && (
                    <div className="w-full p-3 bg-bg-card border border-edge-default rounded-lg">
                      <p className="text-sm text-content-secondary mb-1">Transcribing:</p>
                      <p className="text-content-primary">{transcript}</p>
                    </div>
                  )}
                  {transcript && !isListening && (
                    <button
                      onClick={handleCheckSimilarity}
                      className="px-6 py-2 bg-brand text-white rounded-lg font-medium hover:bg-brand/90 transition-colors"
                    >
                      Check My Answer
                    </button>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-4">
              <div className="inline-flex items-center gap-2 px-4 py-2 bg-bg-card border border-edge-default rounded-full">
                {speechState === 'speaking' ? (
                  <>
                    <div className="w-2 h-2 bg-brand rounded-full animate-pulse"></div>
                    <span className="text-sm text-content-secondary">Speaking...</span>
                  </>
                ) : (
                  <span className="text-sm text-content-secondary">
                    Listen to {currentLine?.character}
                  </span>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  return null;
}
