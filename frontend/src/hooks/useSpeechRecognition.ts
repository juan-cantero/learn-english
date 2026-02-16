import { useState, useRef, useCallback } from 'react';

interface SpeechRecognitionResult {
  transcript: string;
  isListening: boolean;
  isSupported: boolean;
  start: () => void;
  stop: () => void;
  reset: () => void;
  error: string | null;
}

interface SpeechRecognitionEvent {
  results: SpeechRecognitionResultList;
  resultIndex: number;
}

interface SpeechRecognitionResultList {
  readonly length: number;
  item(index: number): SpeechRecognitionResult2;
  [index: number]: SpeechRecognitionResult2;
}

interface SpeechRecognitionResult2 {
  readonly length: number;
  item(index: number): SpeechRecognitionAlternative;
  [index: number]: SpeechRecognitionAlternative;
  readonly isFinal: boolean;
}

interface SpeechRecognitionAlternative {
  readonly transcript: string;
  readonly confidence: number;
}

interface SpeechRecognitionErrorEvent {
  error: string;
  message: string;
}

// Browser SpeechRecognition constructor
type SpeechRecognitionConstructor = new () => SpeechRecognitionInstance;

interface SpeechRecognitionInstance {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onerror: ((event: SpeechRecognitionErrorEvent) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
  abort(): void;
}

function getSpeechRecognition(): SpeechRecognitionConstructor | null {
  if (typeof window === 'undefined') return null;
  return (
    (window as unknown as Record<string, SpeechRecognitionConstructor>).SpeechRecognition ||
    (window as unknown as Record<string, SpeechRecognitionConstructor>).webkitSpeechRecognition ||
    null
  );
}

export function useSpeechRecognition(lang = 'en-US'): SpeechRecognitionResult {
  const [transcript, setTranscript] = useState('');
  const [isListening, setIsListening] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const recognitionRef = useRef<SpeechRecognitionInstance | null>(null);

  const SpeechRecognitionClass = getSpeechRecognition();
  const isSupported = SpeechRecognitionClass !== null;

  const start = useCallback(() => {
    if (!SpeechRecognitionClass) {
      setError('Speech recognition is not supported in this browser');
      return;
    }

    setError(null);
    setTranscript('');

    const recognition = new SpeechRecognitionClass();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = lang;

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      let finalTranscript = '';
      let interimTranscript = '';

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i];
        if (result.isFinal) {
          finalTranscript += result[0].transcript;
        } else {
          interimTranscript += result[0].transcript;
        }
      }

      setTranscript(finalTranscript || interimTranscript);
    };

    recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
      if (event.error !== 'aborted') {
        setError(event.error);
      }
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = recognition;
    recognition.start();
    setIsListening(true);
  }, [SpeechRecognitionClass, lang]);

  const stop = useCallback(() => {
    if (recognitionRef.current) {
      recognitionRef.current.stop();
      recognitionRef.current = null;
    }
    setIsListening(false);
  }, []);

  const reset = useCallback(() => {
    stop();
    setTranscript('');
    setError(null);
  }, [stop]);

  return {
    transcript,
    isListening,
    isSupported,
    start,
    stop,
    reset,
    error,
  };
}

/**
 * Compare two strings and return a similarity percentage (0-100).
 * Uses a simple word-overlap algorithm.
 */
export function calculateSimilarity(expected: string, spoken: string): number {
  const normalize = (s: string) =>
    s
      .toLowerCase()
      .replace(/[^\w\s]/g, '')
      .trim()
      .split(/\s+/);

  const expectedWords = normalize(expected);
  const spokenWords = normalize(spoken);

  if (expectedWords.length === 0) return 0;
  if (spokenWords.length === 0) return 0;

  let matches = 0;
  const spokenSet = new Set(spokenWords);

  for (const word of expectedWords) {
    if (spokenSet.has(word)) {
      matches++;
    }
  }

  return Math.round((matches / expectedWords.length) * 100);
}
