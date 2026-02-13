import { useState, useEffect } from 'react';
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis';

interface AudioPlayerProps {
  text: string;
  size?: 'sm' | 'md';
  showSpeedControl?: boolean;
  className?: string;
}

type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;

const SPEEDS: PlaybackSpeed[] = [0.5, 0.75, 1, 1.25, 1.5];

export function AudioPlayer({
  text,
  size = 'md',
  showSpeedControl = false,
  className = '',
}: AudioPlayerProps) {
  const { state, speak, pause, resume, stop, rate, setRate } = useSpeechSynthesis();
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);

  const sizeClasses = {
    sm: 'h-8 w-8',
    md: 'h-10 w-10',
  };

  const iconSizes = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
  };

  // Close speed menu when clicking outside
  useEffect(() => {
    if (!showSpeedMenu) return;

    const handleClickOutside = () => setShowSpeedMenu(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [showSpeedMenu]);

  const handlePlay = () => {
    if (state === 'speaking') {
      pause();
      return;
    }

    if (state === 'paused') {
      resume();
      return;
    }

    speak(text);
  };

  const handleRetry = () => {
    stop();
    speak(text);
  };

  const handleRepeat = () => {
    stop();
    speak(text);
  };

  const handleSpeedChange = (newSpeed: PlaybackSpeed) => {
    setRate(newSpeed);
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
        className={`flex items-center justify-center rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-brand focus:ring-offset-2 focus:ring-offset-bg-card ${sizeClasses[size]} ${
          state === 'error'
            ? 'bg-error/10 text-error hover:bg-error/20'
            : state === 'speaking'
              ? 'bg-brand-muted text-brand'
              : 'text-content-secondary hover:bg-bg-elevated hover:text-brand'
        }`}
        title={
          state === 'error'
            ? 'Retry'
            : state === 'speaking'
              ? 'Pause'
              : state === 'paused'
                ? 'Resume'
                : 'Play'
        }
      >
        {state === 'error' ? (
          <svg className={iconSizes[size]} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
        ) : state === 'speaking' ? (
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

      {/* Repeat Button - only show when speaking or paused */}
      {(state === 'speaking' || state === 'paused') && (
        <button
          onClick={handleRepeat}
          className={`flex items-center justify-center rounded-lg text-content-secondary transition-colors hover:bg-bg-elevated hover:text-brand focus:outline-none ${sizeClasses[size]}`}
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
            className={`flex items-center justify-center rounded-lg px-2 font-mono text-xs text-content-secondary transition-colors hover:bg-bg-elevated hover:text-brand focus:outline-none ${size === 'sm' ? 'h-8' : 'h-10'}`}
            title="Playback speed"
          >
            {rate}x
          </button>

          {showSpeedMenu && (
            <div className="absolute bottom-full left-0 z-10 mb-1 rounded-lg border border-edge-default bg-bg-inset py-1 shadow-lg">
              {SPEEDS.map((s) => (
                <button
                  key={s}
                  onClick={() => handleSpeedChange(s)}
                  className={`block w-full px-4 py-1.5 text-left font-mono text-sm transition-colors hover:bg-bg-elevated ${
                    rate === s ? 'text-brand' : 'text-content-secondary'
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
