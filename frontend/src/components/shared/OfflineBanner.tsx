import { useOnlineStatus } from '../../hooks/useOnlineStatus';

export function OfflineBanner() {
  const isOnline = useOnlineStatus();

  if (isOnline) return null;

  return (
    <div
      role="alert"
      className="bg-warning-muted text-warning border-b border-warning/20 px-4 py-2 text-center text-sm font-medium"
    >
      You're offline. Some features may be unavailable.
    </div>
  );
}
