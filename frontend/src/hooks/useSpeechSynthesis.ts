import { useState, useRef, useCallback, useEffect } from 'react';

type SpeechState = 'idle' | 'speaking' | 'paused' | 'error';
type PlaybackSpeed = 0.5 | 0.75 | 1 | 1.25 | 1.5;

interface UseSpeechSynthesisOptions {
  rate?: PlaybackSpeed;
  onEnd?: () => void;
}

interface UseSpeechSynthesisReturn {
  state: SpeechState;
  speak: (text: string) => void;
  pause: () => void;
  resume: () => void;
  stop: () => void;
  setRate: (rate: PlaybackSpeed) => void;
  rate: PlaybackSpeed;
  supported: boolean;
}

// Module-level voice cache — shared across all hook instances
let cachedVoice: SpeechSynthesisVoice | null = null;
let voicesLoaded = false;

function loadVoices() {
  const voices = speechSynthesis.getVoices();
  if (voices.length === 0) return;

  voicesLoaded = true;
  // Prefer remote (higher quality) English voices, fall back to local
  const remote = voices.find((v) => v.lang.startsWith('en') && !v.localService);
  if (remote) {
    cachedVoice = remote;
    return;
  }
  cachedVoice = voices.find((v) => v.lang.startsWith('en')) ?? null;
}

// Try loading immediately (works in Firefox)
loadVoices();

// Listen for async voice loading (required in Chrome)
if (typeof speechSynthesis !== 'undefined') {
  speechSynthesis.addEventListener('voiceschanged', loadVoices);
}

export function useSpeechSynthesis(
  options: UseSpeechSynthesisOptions = {},
): UseSpeechSynthesisReturn {
  const [state, setState] = useState<SpeechState>('idle');
  const [rate, setRate] = useState<PlaybackSpeed>(options.rate ?? 1);
  const utteranceRef = useRef<SpeechSynthesisUtterance | null>(null);
  const onEndRef = useRef(options.onEnd);
  const supported = typeof speechSynthesis !== 'undefined';

  // Keep onEnd callback ref fresh
  useEffect(() => {
    onEndRef.current = options.onEnd;
  }, [options.onEnd]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      speechSynthesis.cancel();
    };
  }, []);

  const speak = useCallback(
    (text: string) => {
      if (!supported) {
        setState('error');
        return;
      }

      speechSynthesis.cancel();

      // Ensure voices are loaded (retry in case voiceschanged hasn't fired yet)
      if (!voicesLoaded) {
        loadVoices();
      }

      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = rate;

      if (cachedVoice) {
        utterance.voice = cachedVoice;
      }

      utterance.onstart = () => setState('speaking');
      utterance.onend = () => {
        setState('idle');
        onEndRef.current?.();
      };
      utterance.onerror = (e) => {
        // 'canceled' is not a real error — it fires when we call cancel() before speaking again
        if (e.error === 'canceled') return;
        setState('error');
      };

      utteranceRef.current = utterance;
      speechSynthesis.speak(utterance);
    },
    [rate, supported],
  );

  const pause = useCallback(() => {
    speechSynthesis.pause();
    setState('paused');
  }, []);

  const resume = useCallback(() => {
    speechSynthesis.resume();
    setState('speaking');
  }, []);

  const stop = useCallback(() => {
    speechSynthesis.cancel();
    setState('idle');
  }, []);

  return { state, speak, pause, resume, stop, setRate, rate, supported };
}
