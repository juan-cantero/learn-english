import { useState } from 'react';
import type { Genre } from '../../types/show';

interface EpisodeSelectorProps {
  onSelect: (season: number, episode: number, genre: Genre) => void;
  selectedSeason: number | null;
  selectedEpisode: number | null;
  selectedGenre: Genre | null;
}

const GENRES: Genre[] = [
  'DRAMA',
  'COMEDY',
  'THRILLER',
  'MEDICAL',
  'CRIME',
  'DOCUMENTARY',
  'ANIMATION',
  'SCIENCE_FICTION',
  'FANTASY',
  'ACTION',
];

const GENRE_LABELS: Record<Genre, string> = {
  DRAMA: 'Drama',
  COMEDY: 'Comedy',
  THRILLER: 'Thriller',
  MEDICAL: 'Medical',
  CRIME: 'Crime',
  DOCUMENTARY: 'Documentary',
  ANIMATION: 'Animation',
  SCIENCE_FICTION: 'Science Fiction',
  FANTASY: 'Fantasy',
  ACTION: 'Action',
};

export function EpisodeSelector({
  onSelect,
  selectedSeason,
  selectedEpisode,
  selectedGenre
}: EpisodeSelectorProps) {
  const [season, setSeason] = useState<number>(selectedSeason || 1);
  const [episode, setEpisode] = useState<number>(selectedEpisode || 1);
  const [genre, setGenre] = useState<Genre>(selectedGenre || 'DRAMA');

  const handleSeasonChange = (value: string) => {
    const newSeason = parseInt(value, 10);
    setSeason(newSeason);
    onSelect(newSeason, episode, genre);
  };

  const handleEpisodeChange = (value: string) => {
    const newEpisode = parseInt(value, 10);
    setEpisode(newEpisode);
    onSelect(season, newEpisode, genre);
  };

  const handleGenreChange = (value: string) => {
    const newGenre = value as Genre;
    setGenre(newGenre);
    onSelect(season, episode, newGenre);
  };

  return (
    <div className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-3">
        <div>
          <label htmlFor="season" className="mb-2 block text-sm font-medium text-text-primary">
            Season
          </label>
          <input
            id="season"
            type="number"
            min="1"
            max="20"
            value={season}
            onChange={(e) => handleSeasonChange(e.target.value)}
            className="w-full rounded-lg border border-border bg-bg-card px-4 py-2 font-mono text-text-primary transition-colors focus:border-accent-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/50"
          />
        </div>

        <div>
          <label htmlFor="episode" className="mb-2 block text-sm font-medium text-text-primary">
            Episode
          </label>
          <input
            id="episode"
            type="number"
            min="1"
            max="50"
            value={episode}
            onChange={(e) => handleEpisodeChange(e.target.value)}
            className="w-full rounded-lg border border-border bg-bg-card px-4 py-2 font-mono text-text-primary transition-colors focus:border-accent-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/50"
          />
        </div>

        <div>
          <label htmlFor="genre" className="mb-2 block text-sm font-medium text-text-primary">
            Genre
          </label>
          <select
            id="genre"
            value={genre}
            onChange={(e) => handleGenreChange(e.target.value)}
            className="w-full rounded-lg border border-border bg-bg-card px-4 py-2 text-text-primary transition-colors focus:border-accent-primary focus:outline-none focus:ring-2 focus:ring-accent-primary/50"
          >
            {GENRES.map((g) => (
              <option key={g} value={g}>
                {GENRE_LABELS[g]}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="rounded-lg border border-border bg-bg-card p-4">
        <p className="text-sm text-text-secondary">
          Selected: <span className="font-mono text-text-primary">S{season.toString().padStart(2, '0')}E{episode.toString().padStart(2, '0')}</span> - {GENRE_LABELS[genre]}
        </p>
      </div>
    </div>
  );
}
