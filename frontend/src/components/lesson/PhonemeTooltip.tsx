import { useState, useRef, useEffect, useCallback } from 'react';
import { PHONEMES } from '../../data/phonemes';
import { useSpeechSynthesis } from '../../hooks/useSpeechSynthesis';

const phonemeMap = new Map(PHONEMES.map((p) => [p.symbol, p]));

interface PhonemeTooltipProps {
  symbol: string;
}

export function PhonemeTooltip({ symbol }: PhonemeTooltipProps) {
  const [show, setShow] = useState(false);
  const [position, setPosition] = useState<'above' | 'below'>('above');
  const containerRef = useRef<HTMLSpanElement>(null);
  const hideTimeoutRef = useRef<ReturnType<typeof setTimeout>>(undefined);
  const tts = useSpeechSynthesis();

  const phoneme = phonemeMap.get(symbol);

  const updatePosition = useCallback(() => {
    if (!containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    setPosition(rect.top < 220 ? 'below' : 'above');
  }, []);

  // Hover handlers (desktop)
  const handleMouseEnter = useCallback(() => {
    clearTimeout(hideTimeoutRef.current);
    updatePosition();
    setShow(true);
  }, [updatePosition]);

  const handleMouseLeave = useCallback(() => {
    hideTimeoutRef.current = setTimeout(() => setShow(false), 150);
  }, []);

  // Tap handler (mobile) — toggle on tap
  const handleTap = useCallback(
    (e: React.MouseEvent | React.TouchEvent) => {
      e.preventDefault();
      e.stopPropagation();
      if (show) {
        setShow(false);
      } else {
        updatePosition();
        setShow(true);
      }
    },
    [show, updatePosition],
  );

  // Close on tap outside
  useEffect(() => {
    if (!show) return;

    const handleClickOutside = (e: MouseEvent | TouchEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setShow(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('touchstart', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('touchstart', handleClickOutside);
    };
  }, [show]);

  useEffect(() => {
    return () => clearTimeout(hideTimeoutRef.current);
  }, []);

  const handleSpeak = useCallback(
    (word: string, e: React.MouseEvent) => {
      e.stopPropagation();
      tts.speak(word);
    },
    [tts],
  );

  return (
    <span
      className="relative inline-flex"
      ref={containerRef}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <span
        onClick={handleTap}
        className="inline-flex cursor-pointer items-center rounded-full bg-brand-muted px-2 py-0.5 font-mono text-xs text-brand transition-colors hover:bg-brand/20 active:bg-brand/30"
      >
        {symbol}
      </span>

      {show && phoneme && (
        <div
          className={`absolute left-1/2 z-50 w-72 -translate-x-1/2 rounded-xl border border-edge-default bg-bg-card p-4 shadow-lg ${
            position === 'above' ? 'bottom-full mb-2' : 'top-full mt-2'
          }`}
        >
          {/* Header */}
          <div className="mb-2 flex items-center gap-2">
            <span className="font-mono text-xl text-brand">{phoneme.symbol}</span>
            <span className="text-sm text-content-secondary">{phoneme.name}</span>
          </div>

          {/* Example words with play buttons */}
          <div className="mb-3 flex flex-wrap gap-1.5">
            {phoneme.keywords.map((word) => (
              <button
                key={word}
                onClick={(e) => handleSpeak(word, e)}
                className="group flex items-center gap-1 rounded-lg border border-edge-default bg-bg-inset px-2 py-1 text-xs transition-colors hover:border-brand/50 hover:text-brand active:bg-brand/10"
              >
                <svg
                  className="h-3 w-3 text-content-secondary group-hover:text-brand"
                  fill="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path d="M8 5v14l11-7z" />
                </svg>
                <span className="text-content-primary">{word}</span>
              </button>
            ))}
          </div>

          {/* Description */}
          <p className="mb-2 text-xs leading-relaxed text-content-secondary">
            {phoneme.description}
          </p>

          {/* Spanish tip */}
          <div className="rounded-lg border border-warning/20 bg-warning/5 px-2.5 py-1.5">
            <p className="text-xs text-content-secondary">
              <span className="font-medium text-warning">Tip: </span>
              {phoneme.spanishTip}
            </p>
          </div>
        </div>
      )}
    </span>
  );
}
