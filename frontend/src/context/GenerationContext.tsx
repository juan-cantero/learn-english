import { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';
import type { ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { GenerationStatus } from '../types/generation';
import { getGenerationStatus } from '../api/generation';

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

const POLLING_INTERVAL = 2000; // 2 seconds

export function GenerationProvider({ children }: GenerationProviderProps) {
  const [state, setState] = useState<GenerationState>({ activeJob: null });
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const queryClient = useQueryClient();

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

  // Polling effect
  useEffect(() => {
    const activeJob = state.activeJob;

    // Only poll if there's an active job that's not completed or failed
    if (!activeJob || activeJob.status === 'COMPLETED' || activeJob.status === 'FAILED') {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
      return;
    }

    const pollStatus = async () => {
      try {
        const jobStatus = await getGenerationStatus(activeJob.jobId);
        updateJob({
          progress: jobStatus.progress,
          currentStep: jobStatus.currentStep,
          status: jobStatus.status,
          episodeId: jobStatus.episodeId,
          error: jobStatus.error,
        });

        // Invalidate queries when generation completes so new data is fetched
        if (jobStatus.status === 'COMPLETED') {
          queryClient.invalidateQueries({ queryKey: ['shows'] });
          queryClient.invalidateQueries({ queryKey: ['lessons'] });
        }
      } catch (error) {
        console.error('Failed to poll job status:', error);
        // Don't stop polling on network errors, let it retry
      }
    };

    // Poll immediately on start
    pollStatus();

    // Set up interval for subsequent polls
    pollingRef.current = setInterval(pollStatus, POLLING_INTERVAL);

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
        pollingRef.current = null;
      }
    };
  }, [state.activeJob?.jobId, state.activeJob?.status, updateJob]);

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
