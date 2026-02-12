import { useTheme } from '../../context/ThemeContext';
import type { ThemeSetting } from '../../context/ThemeContext';

const cycle: Record<string, string> = {
  light: 'system',
  system: 'dark',
  dark: 'light',
};

const labels: Record<string, string> = {
  light: 'Light',
  system: 'System',
  dark: 'Dark',
};

function SunIcon() {
  return (
    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
    </svg>
  );
}

function MonitorIcon() {
  return (
    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
    </svg>
  );
}

const icons: Record<string, () => JSX.Element> = {
  light: SunIcon,
  system: MonitorIcon,
  dark: MoonIcon,
};

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();
  const Icon = icons[theme];
  const nextTheme = cycle[theme];

  return (
    <button
      onClick={() => setTheme(nextTheme as ThemeSetting)}
      className="flex h-9 w-9 items-center justify-center rounded-lg text-content-secondary transition-colors hover:bg-bg-elevated hover:text-content-primary"
      title={`Theme: ${labels[theme]} (click for ${labels[nextTheme]})`}
      aria-label={`Switch theme to ${labels[nextTheme]}`}
    >
      <Icon />
    </button>
  );
}
