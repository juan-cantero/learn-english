import { Link } from '@tanstack/react-router';
import { useGenerationContext } from '../../context/GenerationContext';

export function GenerationIndicator() {
  const { state, clearGeneration, hasActiveJob, isCompleted, isFailed } = useGenerationContext();
  const job = state.activeJob;

  if (!hasActiveJob || !job) {
    return null;
  }

  const episodeSlug = `${job.showSlug}-s${job.season}e${job.episode}`;
  const lessonUrl = `/shows/${job.showSlug}/episodes/${episodeSlug}`;

  // Completed state
  if (isCompleted) {
    return (
      <div className="flex items-center gap-3 rounded-lg bg-success/10 px-3 py-1.5">
        <div className="flex items-center gap-2">
          <svg className="h-4 w-4 text-success" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <span className="text-sm text-success">Ready!</span>
        </div>
        <Link
          to={lessonUrl}
          onClick={clearGeneration}
          className="text-sm font-medium text-accent-primary hover:text-accent-primary/80"
        >
          Go to lesson
        </Link>
        <button
          onClick={clearGeneration}
          className="ml-1 text-text-secondary hover:text-text-primary"
          aria-label="Dismiss"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    );
  }

  // Failed state
  if (isFailed) {
    return (
      <div className="flex items-center gap-3 rounded-lg bg-error/10 px-3 py-1.5">
        <div className="flex items-center gap-2">
          <svg className="h-4 w-4 text-error" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
          <span className="text-sm text-error">Generation failed</span>
        </div>
        <button
          onClick={clearGeneration}
          className="text-sm font-medium text-text-secondary hover:text-text-primary"
        >
          Dismiss
        </button>
      </div>
    );
  }

  // Processing state
  return (
    <div className="flex items-center gap-3 rounded-lg bg-accent-primary/10 px-3 py-1.5">
      <div className="h-4 w-4 animate-spin rounded-full border-2 border-accent-primary border-t-transparent" />
      <span className="text-sm text-text-primary">
        Generating {job.showTitle} S{job.season}E{job.episode}...
      </span>
      <span className="font-mono text-sm text-accent-primary">{job.progress}%</span>
    </div>
  );
}
