import type { TMDBShow } from '../../types/generation';

interface SearchShowCardProps {
  show: TMDBShow;
  isSelected: boolean;
  onSelect: (show: TMDBShow) => void;
}

export function SearchShowCard({ show, isSelected, onSelect }: SearchShowCardProps) {
  const posterUrl = show.posterPath
    ? `https://image.tmdb.org/t/p/w342${show.posterPath}`
    : null;

  return (
    <button
      onClick={() => onSelect(show)}
      className={`group block w-full overflow-hidden rounded-xl bg-bg-card text-left transition-all hover:bg-bg-card-hover hover:scale-[1.02] hover:shadow-xl hover:shadow-accent-primary/10 focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark ${
        isSelected ? 'ring-2 ring-accent-primary' : 'hover:ring-1 hover:ring-accent-primary/50'
      }`}
    >
      <div className="aspect-[2/3] overflow-hidden bg-bg-dark">
        {posterUrl ? (
          <img
            src={posterUrl}
            alt={show.title}
            className="h-full w-full object-cover transition-transform group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center">
            <span className="text-4xl font-bold text-text-secondary">
              {show.title.charAt(0)}
            </span>
          </div>
        )}
      </div>

      <div className="p-4">
        <h3 className="mb-1 text-lg font-semibold text-text-primary group-hover:text-accent-primary">
          {show.title}
        </h3>

        <p className="mb-2 font-mono text-xs text-text-secondary">
          {show.year}
        </p>

        <p className="line-clamp-3 text-sm text-text-secondary">
          {show.overview}
        </p>
      </div>
    </button>
  );
}
