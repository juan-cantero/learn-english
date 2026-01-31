import type { Genre } from '../../types/show';

const GENRES: { value: Genre; label: string }[] = [
  { value: 'DRAMA', label: 'Drama' },
  { value: 'COMEDY', label: 'Comedy' },
  { value: 'ACTION', label: 'Action' },
  { value: 'THRILLER', label: 'Thriller' },
  { value: 'CRIME', label: 'Crime' },
  { value: 'MEDICAL', label: 'Medical' },
  { value: 'SCIENCE_FICTION', label: 'Sci-Fi' },
  { value: 'FANTASY', label: 'Fantasy' },
  { value: 'DOCUMENTARY', label: 'Documentary' },
  { value: 'ANIMATION', label: 'Animation' },
];

interface GenreSelectorProps {
  value: Genre;
  onChange: (genre: Genre) => void;
}

export function GenreSelector({ value, onChange }: GenreSelectorProps) {
  return (
    <div className="flex flex-wrap gap-2">
      {GENRES.map((genre) => (
        <button
          key={genre.value}
          type="button"
          onClick={() => onChange(genre.value)}
          className={`rounded-full px-4 py-2 text-sm font-medium transition-all focus:outline-none focus:ring-2 focus:ring-accent-primary focus:ring-offset-2 focus:ring-offset-bg-dark ${
            value === genre.value
              ? 'bg-accent-primary text-white shadow-md shadow-accent-primary/25'
              : 'bg-bg-card text-text-secondary hover:bg-bg-card-hover hover:text-text-primary'
          }`}
        >
          {genre.label}
        </button>
      ))}
    </div>
  );
}
