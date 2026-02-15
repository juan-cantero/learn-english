import { useState } from 'react';
import { useInstallPrompt } from '../../hooks/useInstallPrompt';

export function InstallBanner() {
  const { canInstall, install } = useInstallPrompt();
  const [dismissed, setDismissed] = useState(false);

  if (!canInstall || dismissed) return null;

  return (
    <div className="border-b border-brand/20 bg-brand-muted px-4 py-2.5">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-3">
        <p className="text-sm font-medium text-content-primary">
          Install LearnTV for a better experience
        </p>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setDismissed(true)}
            className="rounded-lg px-3 py-1.5 text-xs font-medium text-content-secondary transition-colors hover:text-content-primary"
          >
            Not now
          </button>
          <button
            onClick={install}
            className="rounded-lg bg-brand px-3 py-1.5 text-xs font-medium text-white transition-colors hover:bg-brand-hover"
          >
            Install
          </button>
        </div>
      </div>
    </div>
  );
}
