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
      <div className={`w-full overflow-hidden rounded-full bg-bg-dark ${heights[size]}`}>
        <div
          className="h-full rounded-full bg-accent-primary transition-all duration-500"
          style={{ width: `${clampedPercentage}%` }}
        />
      </div>
      {showLabel && (
        <span className="mt-1 block text-right font-mono text-xs text-text-secondary">
          {Math.round(clampedPercentage)}%
        </span>
      )}
    </div>
  );
}
