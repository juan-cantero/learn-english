import { useEffect } from 'react';
import { useGenerationStatus } from '../../hooks/useGeneration';
import type { GenerationStatus } from '../../types/generation';

interface GenerationProgressProps {
  jobId: string;
  onComplete: (episodeId: string) => void;
  onError: () => void;
}

const progressSteps: Record<number, string> = {
  10: 'Fetching episode information...',
  20: 'Downloading script...',
  40: 'Analyzing vocabulary...',
  55: 'Identifying grammar patterns...',
  70: 'Extracting expressions...',
  80: 'Creating exercises...',
  90: 'Generating pronunciation audio...',
  95: 'Saving lesson...',
  100: 'Complete!',
};

function getStepText(progress: number, currentStep: string): string {
  // Use currentStep from backend if available
  if (currentStep && currentStep !== 'Starting...') {
    return currentStep;
  }

  // Otherwise find the closest progress step
  const progressKeys = Object.keys(progressSteps)
    .map(Number)
    .sort((a, b) => a - b);

  for (let i = progressKeys.length - 1; i >= 0; i--) {
    if (progress >= progressKeys[i]) {
      return progressSteps[progressKeys[i]];
    }
  }

  return 'Starting...';
}

function StatusIcon({ status }: { status: GenerationStatus }) {
  if (status === 'COMPLETED') {
    return (
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-success/20">
        <svg
          className="h-8 w-8 text-success"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
      </div>
    );
  }

  if (status === 'FAILED') {
    return (
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-error/20">
        <svg
          className="h-8 w-8 text-error"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </div>
    );
  }

  // PENDING or PROCESSING
  return (
    <div className="flex h-16 w-16 items-center justify-center">
      <svg className="h-12 w-12 animate-spin text-accent-primary" viewBox="0 0 24 24">
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="4"
          fill="none"
        />
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </div>
  );
}

export function GenerationProgress({ jobId, onComplete, onError }: GenerationProgressProps) {
  const { data: job, isLoading } = useGenerationStatus(jobId);

  useEffect(() => {
    if (!job) return;

    if (job.status === 'COMPLETED' && job.episodeId) {
      onComplete(job.episodeId);
    } else if (job.status === 'FAILED') {
      onError();
    }
  }, [job, onComplete, onError]);

  if (isLoading || !job) {
    return (
      <div className="flex flex-col items-center justify-center space-y-6 rounded-xl border border-border bg-bg-card p-12">
        <StatusIcon status="PENDING" />
        <div className="text-center">
          <h3 className="text-xl font-semibold text-text-primary">Loading...</h3>
          <p className="mt-2 text-sm text-text-secondary">Fetching generation status...</p>
        </div>
      </div>
    );
  }

  const stepText = getStepText(job.progress, job.currentStep);

  return (
    <div className="flex flex-col items-center justify-center space-y-6 rounded-xl border border-border bg-bg-card p-12">
      {/* Status Icon */}
      <StatusIcon status={job.status} />

      {/* Status Text */}
      <div className="w-full max-w-md text-center">
        {job.status === 'COMPLETED' ? (
          <>
            <h3 className="text-2xl font-bold text-success">Lesson Ready!</h3>
            <p className="mt-2 text-text-secondary">
              Your lesson has been generated successfully.
            </p>
          </>
        ) : job.status === 'FAILED' ? (
          <>
            <h3 className="text-2xl font-bold text-error">Generation Failed</h3>
            <p className="mt-2 text-text-secondary">
              {job.error || 'An unexpected error occurred during generation.'}
            </p>
          </>
        ) : (
          <>
            <h3 className="text-2xl font-bold text-text-primary">Generating Lesson</h3>
            <p className="mt-2 text-text-secondary transition-all duration-300">{stepText}</p>
          </>
        )}
      </div>

      {/* Progress Bar (only show for PENDING/PROCESSING) */}
      {(job.status === 'PENDING' || job.status === 'PROCESSING') && (
        <div className="w-full max-w-md space-y-2" role="progressbar" aria-valuenow={job.progress} aria-valuemin={0} aria-valuemax={100}>
          <div className="h-2 overflow-hidden rounded-full bg-border">
            <div
              className="h-full rounded-full bg-accent-primary transition-all duration-500 ease-out"
              style={{ width: `${job.progress}%` }}
            />
          </div>
          <p className="text-center text-sm font-mono text-text-secondary">{job.progress}%</p>
        </div>
      )}

      {/* Action Buttons */}
      {job.status === 'COMPLETED' && job.episodeId && (
        <button
          onClick={() => onComplete(job.episodeId!)}
          className="mt-4 rounded-lg bg-accent-primary px-8 py-3 font-semibold text-white transition-all hover:bg-accent-secondary focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark"
        >
          View Lesson
        </button>
      )}

      {job.status === 'FAILED' && (
        <button
          onClick={onError}
          className="mt-4 rounded-lg border border-border bg-bg-dark px-8 py-3 font-semibold text-text-primary transition-all hover:bg-bg-card-hover focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark"
        >
          Try Again
        </button>
      )}

      {/* Job ID for debugging */}
      <p className="mt-4 text-xs font-mono text-text-secondary opacity-50">
        Job ID: {jobId}
      </p>
    </div>
  );
}
