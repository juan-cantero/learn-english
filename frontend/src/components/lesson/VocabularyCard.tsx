import { useState, useRef } from 'react';
import type { Vocabulary } from '../../types/lesson';

interface VocabularyCardProps {
  vocabulary: Vocabulary;
}

const categoryColors = {
  MEDICAL: 'bg-red-900/30 text-red-400 border-red-800/50',
  TECHNICAL: 'bg-blue-900/30 text-blue-400 border-blue-800/50',
  SLANG: 'bg-purple-900/30 text-purple-400 border-purple-800/50',
  IDIOM: 'bg-yellow-900/30 text-yellow-400 border-yellow-800/50',
  PROFESSIONAL: 'bg-cyan-900/30 text-cyan-400 border-cyan-800/50',
  EVERYDAY: 'bg-green-900/30 text-green-400 border-green-800/50',
  EMOTIONAL: 'bg-pink-900/30 text-pink-400 border-pink-800/50',
  COLLOQUIAL: 'bg-orange-900/30 text-orange-400 border-orange-800/50',
  ACTION: 'bg-indigo-900/30 text-indigo-400 border-indigo-800/50',
};

export function VocabularyCard({ vocabulary }: VocabularyCardProps) {
  const [isPlaying, setIsPlaying] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const handleSpeak = async () => {
    if (isPlaying) return;

    setIsPlaying(true);
    setError(null);

    try {
      let audioUrl: string;

      if (vocabulary.audioUrl) {
        // Use pre-generated audio from R2/local storage
        audioUrl = vocabulary.audioUrl;
      } else {
        // Fallback to TTS API (for development/old data)
        audioUrl = `http://localhost:8080/api/v1/tts/speak?text=${encodeURIComponent(vocabulary.term)}`;
      }

      if (audioRef.current) {
        audioRef.current.pause();
      }

      const audio = new Audio(audioUrl);
      audioRef.current = audio;

      audio.onended = () => {
        setIsPlaying(false);
        setError(null);
      };

      audio.onerror = () => {
        setError('Failed to play audio');
        setIsPlaying(false);
      };

      await audio.play();
    } catch (error) {
      console.error('Audio playback error:', error);
      setError('Failed to play audio');
      setIsPlaying(false);
    }
  };

  return (
    <div className="rounded-xl border border-border bg-bg-card p-5 transition-colors hover:border-accent-primary/30">
      <div className="mb-3 flex items-start justify-between gap-3">
        <div>
          <h4 className="text-lg font-semibold text-text-primary">{vocabulary.term}</h4>
          {vocabulary.phonetic && (
            <p className="font-mono text-sm text-text-secondary">{vocabulary.phonetic}</p>
          )}
        </div>
        <div className="flex items-center gap-2">
          <span
            className={`rounded-md border px-2 py-1 text-xs ${categoryColors[vocabulary.category]}`}
          >
            {vocabulary.category}
          </span>
          <button
            onClick={handleSpeak}
            disabled={isPlaying}
            className={`rounded-lg p-3 transition-colors focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-card ${
              isPlaying
                ? 'text-accent-primary bg-accent-primary/10'
                : 'text-text-secondary hover:bg-bg-dark hover:text-accent-primary'
            }`}
            title="Listen to pronunciation"
          >
            {isPlaying ? (
              <svg className="h-4 w-4 animate-pulse" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z"/>
              </svg>
            ) : (
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"
                />
              </svg>
            )}
          </button>
        </div>
      </div>

      <p className="text-text-primary">{vocabulary.definition}</p>

      {error && (
        <div className="mt-3 rounded-lg bg-red-900/20 border border-red-800/50 p-2">
          <p className="text-xs text-red-400">{error}</p>
        </div>
      )}

      {vocabulary.exampleSentence && (
        <div className="mt-4 rounded-lg bg-bg-dark p-3">
          <p className="text-sm italic text-text-secondary">"{vocabulary.exampleSentence}"</p>
        </div>
      )}

      {vocabulary.contextTimestamp && (
        <p className="mt-3 font-mono text-xs text-text-secondary">
          Used at: {vocabulary.contextTimestamp}
        </p>
      )}
    </div>
  );
}
