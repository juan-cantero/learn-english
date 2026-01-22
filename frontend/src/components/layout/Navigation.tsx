import { Link } from '@tanstack/react-router';

interface NavigationProps {
  showTitle?: string;
  showSlug?: string;
  episodeTitle?: string;
}

export function Navigation({ showTitle, showSlug, episodeTitle }: NavigationProps) {
  return (
    <nav className="flex items-center gap-2 text-sm text-text-secondary">
      <Link to="/" className="hover:text-text-primary">
        Shows
      </Link>
      {showTitle && (
        <>
          <span>/</span>
          <Link
            to="/shows/$slug"
            params={{ slug: showSlug! }}
            className="hover:text-text-primary"
          >
            {showTitle}
          </Link>
        </>
      )}
      {episodeTitle && (
        <>
          <span>/</span>
          <span className="text-text-primary">{episodeTitle}</span>
        </>
      )}
    </nav>
  );
}
