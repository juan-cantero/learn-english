import { Link } from '@tanstack/react-router';
import { useProgressSnapshot } from '../hooks/useProgress';
import { ProgressBar } from '../components/layout/ProgressBar';
import { EmptyState } from '../components/shared/EmptyState';

export function ProgressPage() {
  const { data: snapshot, isLoading, error, refetch } = useProgressSnapshot();

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse">
          <div className="mb-8 h-8 w-48 rounded bg-bg-card" />
          <div className="grid gap-6 md:grid-cols-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-32 rounded-xl bg-bg-card" />
            ))}
          </div>
          <div className="mt-8 rounded-xl bg-bg-card p-6">
            <div className="mb-6 h-6 w-40 rounded bg-bg-inset" />
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-24 rounded-lg bg-bg-inset" />
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-3xl font-bold text-content-primary">My Progress</h1>
        <EmptyState
          icon="connection"
          title="Failed to load progress"
          description={error.message || "We couldn't connect to the server. Please check your connection and try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      </div>
    );
  }

  if (!snapshot) return null;

  const completionRate =
    snapshot.totalLessonsStarted > 0
      ? Math.round((snapshot.totalLessonsCompleted / snapshot.totalLessonsStarted) * 100)
      : 0;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <h1 className="mb-8 text-3xl font-bold text-content-primary">My Progress</h1>

      <div className="mb-8 grid gap-6 md:grid-cols-3">
        <div className="rounded-xl border border-edge-default bg-bg-card p-6">
          <p className="text-sm text-content-secondary">Total Points</p>
          <p className="mt-2 font-mono text-4xl font-bold text-brand">
            {snapshot.totalPoints.toLocaleString()}
          </p>
        </div>

        <div className="rounded-xl border border-edge-default bg-bg-card p-6">
          <p className="text-sm text-content-secondary">Lessons Started</p>
          <p className="mt-2 font-mono text-4xl font-bold text-content-primary">
            {snapshot.totalLessonsStarted}
          </p>
        </div>

        <div className="rounded-xl border border-edge-default bg-bg-card p-6">
          <p className="text-sm text-content-secondary">Lessons Completed</p>
          <div className="mt-2 flex items-end justify-between">
            <p className="font-mono text-4xl font-bold text-success">
              {snapshot.totalLessonsCompleted}
            </p>
            <span className="font-mono text-lg text-content-secondary">{completionRate}%</span>
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-edge-default bg-bg-card p-6">
        <h2 className="mb-6 text-xl font-semibold text-content-primary">Recent Activity</h2>

        {!snapshot.recentProgress?.length ? (
          <EmptyState
            icon="empty"
            title="No activity yet"
            description="Start a lesson to track your learning progress here."
          >
            <Link
              to="/generate"
              className="mt-4 inline-flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Generate a Lesson
            </Link>
          </EmptyState>
        ) : (
          <div className="space-y-4">
            {snapshot.recentProgress?.map((progress) => {
              const episodeLabel = progress.episode
                ? `S${String(progress.episode.seasonNumber).padStart(2, '0')}E${String(progress.episode.episodeNumber).padStart(2, '0')}`
                : null;
              const episodeTitle = progress.episode?.title || `Episode ${progress.episodeId.slice(0, 8)}...`;

              const content = (
                <div className="flex items-center gap-4">
                  <div
                    className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-lg ${
                      progress.completed ? 'bg-success/20 text-success' : 'bg-brand-muted text-brand'
                    }`}
                  >
                    {progress.completed ? (
                      <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 20 20">
                        <path
                          fillRule="evenodd"
                          d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                          clipRule="evenodd"
                        />
                      </svg>
                    ) : (
                      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                    )}
                  </div>

                  <div className="min-w-0 flex-1">
                    <div className="mb-2 flex items-center justify-between gap-2">
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-medium text-content-primary">
                          {episodeTitle}
                        </p>
                        {episodeLabel && (
                          <p className="text-xs text-content-secondary">{episodeLabel}</p>
                        )}
                      </div>
                      <span className="shrink-0 font-mono text-sm text-brand">
                        {progress.totalPoints} pts
                      </span>
                    </div>
                    <ProgressBar
                      percentage={
                        progress.completed
                          ? 100
                          : Math.min(
                              100,
                              (progress.totalPoints / Math.max(1, progress.totalPoints + 50)) * 100
                            )
                      }
                      size="sm"
                    />
                    <div className="mt-2 flex flex-wrap gap-3 text-xs text-content-secondary">
                      <span>Vocab: {progress.vocabularyScore}</span>
                      <span>Grammar: {progress.grammarScore}</span>
                      <span>Expressions: {progress.expressionsScore}</span>
                      <span>Exercises: {progress.exercisesScore}</span>
                    </div>
                  </div>

                  <div className="flex shrink-0 flex-col items-end gap-1">
                    <p className="text-xs text-content-secondary">
                      {new Date(progress.lastAccessed).toLocaleDateString()}
                    </p>
                    {progress.episode && !progress.completed && (
                      <span className="text-xs text-brand">Continue →</span>
                    )}
                  </div>
                </div>
              );

              // If we have episode metadata, make it a link
              if (progress.episode) {
                return (
                  <Link
                    key={progress.id}
                    to="/shows/$slug/episodes/$episodeSlug"
                    params={{
                      slug: progress.episode.showSlug,
                      episodeSlug: progress.episode.episodeSlug,
                    }}
                    className="block rounded-lg border border-edge-default bg-bg-inset p-4 transition-colors hover:border-brand/50 hover:bg-bg-card"
                  >
                    {content}
                  </Link>
                );
              }

              return (
                <div
                  key={progress.id}
                  className="rounded-lg border border-edge-default bg-bg-inset p-4"
                >
                  {content}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
