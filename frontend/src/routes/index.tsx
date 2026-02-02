import { Link } from '@tanstack/react-router';
import { useShows } from '../hooks/useShows';
import { useProgressSnapshot } from '../hooks/useProgress';
import { ShowCard } from '../components/show/ShowCard';
import { EmptyState } from '../components/shared/EmptyState';
import { ProgressBar } from '../components/layout/ProgressBar';

export function HomePage() {
  const { data: shows, isLoading, error, refetch } = useShows();
  const { data: progress } = useProgressSnapshot();

  // Get in-progress lessons (not completed, with episode metadata)
  const inProgressLessons = progress?.recentProgress
    ?.filter((p) => !p.completed && p.episode)
    .slice(0, 3) ?? [];

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      {/* Continue Learning Section */}
      {inProgressLessons.length > 0 && (
        <div className="mb-10">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-semibold text-text-primary">Continue Learning</h2>
            <Link
              to="/progress"
              className="text-sm text-text-secondary transition-colors hover:text-accent-primary"
            >
              View all →
            </Link>
          </div>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {inProgressLessons.map((lesson) => (
              <Link
                key={lesson.id}
                to="/shows/$slug/episodes/$episodeSlug"
                params={{
                  slug: lesson.episode!.showSlug,
                  episodeSlug: lesson.episode!.episodeSlug,
                }}
                className="group rounded-xl border border-border bg-bg-card p-4 transition-all hover:border-accent-primary/50 hover:bg-bg-card-hover"
              >
                <div className="mb-3 flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="truncate font-medium text-text-primary group-hover:text-accent-primary">
                      {lesson.episode!.title}
                    </p>
                    <p className="text-sm text-text-secondary">
                      S{String(lesson.episode!.seasonNumber).padStart(2, '0')}E
                      {String(lesson.episode!.episodeNumber).padStart(2, '0')}
                    </p>
                  </div>
                  <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-accent-primary/10 text-accent-primary">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </div>
                </div>
                <ProgressBar
                  percentage={Math.min(
                    100,
                    (lesson.totalPoints / Math.max(1, lesson.totalPoints + 50)) * 100
                  )}
                  size="sm"
                />
                <p className="mt-2 text-xs text-text-secondary">
                  {lesson.totalPoints} points earned
                </p>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-text-primary">
          {inProgressLessons.length > 0 ? 'Browse Shows' : 'Learn English with TV Shows'}
        </h1>
        <p className="mt-2 text-text-secondary">
          Choose a show to start learning vocabulary, grammar, and expressions from real dialogue.
        </p>
      </div>

      {isLoading && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="aspect-video rounded-t-xl bg-bg-card" />
              <div className="rounded-b-xl bg-bg-card p-4">
                <div className="mb-2 h-6 w-3/4 rounded bg-bg-dark" />
                <div className="h-4 w-full rounded bg-bg-dark" />
                <div className="mt-2 h-4 w-2/3 rounded bg-bg-dark" />
              </div>
            </div>
          ))}
        </div>
      )}

      {error && (
        <EmptyState
          icon="connection"
          title="Failed to load shows"
          description={error.message || "We couldn't connect to the server. Please check your connection and try again."}
          action={{
            label: 'Try Again',
            onClick: () => refetch(),
          }}
        />
      )}

      {shows && shows.length === 0 && (
        <EmptyState
          icon="tv"
          title="No shows yet"
          description="Generate your first lesson to start learning English from TV shows."
        >
          <Link
            to="/generate"
            className="mt-6 inline-flex items-center gap-2 rounded-lg bg-accent-primary px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-accent-secondary"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Generate a Lesson
          </Link>
        </EmptyState>
      )}

      {shows && shows.length > 0 && (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {shows.map((show) => (
            <ShowCard key={show.id} show={show} />
          ))}
        </div>
      )}
    </div>
  );
}
