import { ShowSearch } from '../components/generation/ShowSearch';

export function GeneratePage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-text-primary">
          Generate New Lesson
        </h1>
        <p className="mt-2 text-text-secondary">
          Search for a TV show to get started. You'll then select a season and episode.
        </p>
      </div>

      <section className="rounded-xl border border-border bg-bg-card p-6">
        <ShowSearch />
      </section>
    </div>
  );
}
