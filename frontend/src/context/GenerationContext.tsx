import { createContext, useContext, useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import type { GenerationStatus } from '../types/generation';

export interface ActiveJob {
  jobId: string;
  tmdbId: string;
  showTitle: string;
  showSlug: string;
  season: number;
  episode: number;
  progress: number;
  currentStep: string;
  status: GenerationStatus;
  episodeId: string | null;
  error?: string;
}

interface GenerationState {
  activeJob: ActiveJob | null;
}

interface GenerationContextValue {
  state: GenerationState;
  startGeneration: (job: Omit<ActiveJob, 'progress' | 'currentStep' | 'status' | 'episodeId'> & { jobId: string }) => void;
  updateJob: (updates: Partial<ActiveJob>) => void;
  clearGeneration: () => void;
  hasActiveJob: boolean;
  isCompleted: boolean;
  isFailed: boolean;
}

const GenerationContext = createContext<GenerationContextValue | null>(null);

interface GenerationProviderProps {
  children: ReactNode;
}

export function GenerationProvider({ children }: GenerationProviderProps) {
  const [state, setState] = useState<GenerationState>({ activeJob: null });

  const startGeneration = useCallback((job: Omit<ActiveJob, 'progress' | 'currentStep' | 'status' | 'episodeId'> & { jobId: string }) => {
    setState({
      activeJob: {
        ...job,
        progress: 0,
        currentStep: 'Starting...',
        status: 'PENDING',
        episodeId: null,
      },
    });
  }, []);

  const updateJob = useCallback((updates: Partial<ActiveJob>) => {
    setState((prev) => {
      if (!prev.activeJob) return prev;
      return {
        activeJob: { ...prev.activeJob, ...updates },
      };
    });
  }, []);

  const clearGeneration = useCallback(() => {
    setState({ activeJob: null });
  }, []);

  const hasActiveJob = state.activeJob !== null;
  const isCompleted = state.activeJob?.status === 'COMPLETED';
  const isFailed = state.activeJob?.status === 'FAILED';

  return (
    <GenerationContext.Provider
      value={{
        state,
        startGeneration,
        updateJob,
        clearGeneration,
        hasActiveJob,
        isCompleted,
        isFailed,
      }}
    >
      {children}
    </GenerationContext.Provider>
  );
}

export function useGenerationContext(): GenerationContextValue {
  const context = useContext(GenerationContext);
  if (!context) {
    throw new Error('useGenerationContext must be used within a GenerationProvider');
  }
  return context;
}
