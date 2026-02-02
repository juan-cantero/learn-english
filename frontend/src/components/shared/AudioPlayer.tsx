import { useState, useRef, useEffect } from 'react';

interface AudioPlayerProps {
  src: string;
  fallbackText?: string; // Text to use with TTS API if src fails
  size?: 'sm' | 'md';
  showSpeedControl?: boolean;
  className?: string;
}

type PlaybackState = 'idle' | 'loading' | 'playing' | 'paused' | 'error';
type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;

const SPEEDS: PlaybackSpeed[] = [0.5, 0.75, 1, 1.25, 1.5];
const TTS_API_BASE = 'http://localhost:8080/api/v1/tts/speak';

export function AudioPlayer({
  src,
  fallbackText,
  size = 'md',
  showSpeedControl = false,
  className = '',
}: AudioPlayerProps) {
  const [state, setState] = useState<PlaybackState>('idle');
  const [speed, setSpeed] = useState<PlaybackSpeed>(1);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const retryCountRef = useRef(0);

  const sizeClasses = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
  };

  const iconSizes = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
  };

  // Cleanup audio on unmount
  useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  // Close speed menu when clicking outside
  useEffect(() => {
    if (!showSpeedMenu) return;

    const handleClickOutside = () => setShowSpeedMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showSpeedMenu]);

  const getAudioUrl = (): string => {
    // If we've failed with the main src and have fallback text, use TTS API
    if (retryCountRef.current > 0 && fallbackText) {
      return `${TTS_API_BASE}?text=${encodeURIComponent(fallbackText)}`;
    }
    return src;
  };

  const handlePlay = async () => {
    if (state === 'loading') return;

    // If playing, pause
    if (state === 'playing' && audioRef.current) {
      audioRef.current.pause();
      setState('paused');
      return;
    }

    // If paused, resume
    if (state === 'paused' && audioRef.current) {
      try {
        await audioRef.current.play();
        setState('playing');
      } catch {
        setState('error');
      }
      return;
    }

    // Start fresh playback
    setState('loading');

    try {
      // Cleanup previous audio
      if (audioRef.current) {
        audioRef.current.pause();
      }

      const audio = new Audio(getAudioUrl());
      audio.playbackRate = speed;
      audioRef.current = audio;

      audio.oncanplaythrough = () => {
        audio.play().catch(() => setState('error'));
        setState('playing');
      };

      audio.onended = () => {
        setState('idle');
        retryCountRef.current = 0;
      };

      audio.onerror = () => {
        // Try fallback if available and not already tried
        if (fallbackText && retryCountRef.current === 0) {
          retryCountRef.current = 1;
          handlePlay(); // Retry with fallback URL
        } else {
          setState('error');
        }
      };

      audio.load();
    } catch {
      setState('error');
    }
  };

  const handleRetry = () => {
    retryCountRef.current = 0;
    setState('idle');
    handlePlay();
  };

  const handleRepeat = () => {
    if (audioRef.current) {
      audioRef.current.currentTime = 0;
      audioRef.current.play().catch(() => setState('error'));
      setState('playing');
    } else {
      handlePlay();
    }
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

  return (
    <div className={`flex items-center gap-1 ${className}`}>
      {/* Play/Pause/Error Button */}
      <button
        onClick={state === 'error' ? handleRetry : handlePlay}
        disabled={state === 'loading'}
        className={`flex items-center justify-center rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-card ${sizeClasses[size]} ${
          state === 'error'
            ? 'bg-error/10 text-error hover:bg-error/20'
            : state === 'playing'
              ? 'bg-accent-primary/10 text-accent-primary'
              : 'text-text-secondary hover:bg-bg-dark hover:text-accent-primary'
        }`}
        title={
          state === 'error'
            ? 'Retry'
            : state === 'playing'
              ? 'Pause'
              : state === 'loading'
                ? 'Loading...'
                : 'Play'
        }
      >
        {state === 'loading' ? (
          <svg className={`${iconSizes[size]} animate-spin`} fill="none" viewBox="0 0 24 24">
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
        ) : state === 'error' ? (
          <svg className={iconSizes[size]} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
        ) : state === 'playing' ? (
          <svg className={iconSizes[size]} fill="currentColor" viewBox="0 0 24 24">
            <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
          </svg>
        ) : (
          <svg className={iconSizes[size]} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"
            />
          </svg>
        )}
      </button>

      {/* Repeat Button - only show when playing or paused */}
      {(state === 'playing' || state === 'paused') && (
        <button
          onClick={handleRepeat}
          className={`flex items-center justify-center rounded-lg text-text-secondary transition-colors hover:bg-bg-dark hover:text-accent-primary focus:outline-none ${sizeClasses[size]}`}
          title="Repeat"
        >
          <svg className={iconSizes[size]} fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
      {showSpeedControl && (
        <div className="relative">
          <button
            onClick={toggleSpeedMenu}
            className={`flex items-center justify-center rounded-lg px-2 font-mono text-xs text-text-secondary transition-colors hover:bg-bg-dark hover:text-accent-primary focus:outline-none ${size === 'sm' ? 'h-8' : 'h-10'}`}
            title="Playback speed"
          >
            {speed}x
          </button>

          {showSpeedMenu && (
            <div className="absolute bottom-full left-0 z-10 mb-1 rounded-lg border border-border bg-bg-card py-1 shadow-lg">
              {SPEEDS.map((s) => (
                <button
                  key={s}
                  onClick={() => handleSpeedChange(s)}
                  className={`block w-full px-4 py-1.5 text-left font-mono text-sm transition-colors hover:bg-bg-dark ${
                    speed === s ? 'text-accent-primary' : 'text-text-secondary'
                  }`}
                >
                  {s}x
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
