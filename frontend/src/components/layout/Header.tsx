import { Link } from '@tanstack/react-router';
import { GenerationIndicator } from './GenerationIndicator';
import { UserMenu } from './UserMenu';

export function Header() {
  return (
    <header className="border-b border-border bg-bg-card">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <Link to="/" className="flex items-center gap-3 no-underline">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent-primary">
              <span className="font-mono text-lg font-bold text-white">TV</span>
            </div>
            <span className="text-xl font-semibold text-text-primary">LearnTV</span>
          </Link>

          <div className="flex items-center gap-6">
            <GenerationIndicator />
            {/* Hide nav links on mobile - use BottomNavigation instead */}
            <nav className="hidden items-center gap-6 md:flex">
              <Link
                to="/"
                className="text-sm font-medium text-text-secondary transition-colors hover:text-text-primary [&.active]:text-accent-primary"
              >
                Browse
              </Link>
              <Link
                to="/generate"
                className="text-sm font-medium text-text-secondary transition-colors hover:text-text-primary [&.active]:text-accent-primary"
              >
                Generate
              </Link>
              <Link
                to="/progress"
                className="text-sm font-medium text-text-secondary transition-colors hover:text-text-primary [&.active]:text-accent-primary"
              >
                My Progress
              </Link>
            </nav>
            <UserMenu />
          </div>
        </div>
      </div>
    </header>
  );
}
