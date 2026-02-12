interface ProgressBarProps {
  percentage: number;
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
}

export function ProgressBar({ percentage, size = 'md', showLabel = false }: ProgressBarProps) {
  const heights = {
    sm: 'h-1',
    md: 'h-2',
    lg: 'h-3',
  };

  const clampedPercentage = Math.min(100, Math.max(0, percentage));

  return (
    <div className="w-full">
      <div className={`w-full overflow-hidden rounded-full bg-bg-inset ${heights[size]}`}>
        <div
          className="h-full rounded-full bg-brand transition-all duration-500"
          style={{ width: `${clampedPercentage}%` }}
        />
      </div>
      {showLabel && (
        <span className="mt-1 block text-right font-mono text-xs text-content-secondary">
          {Math.round(clampedPercentage)}%
        </span>
      )}
    </div>
  );
}
