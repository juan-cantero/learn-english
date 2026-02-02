import type { Expression } from '../../types/lesson';
import { AudioPlayer } from '../shared/AudioPlayer';

interface ExpressionCardProps {
  expression: Expression;
}

export function ExpressionCard({ expression }: ExpressionCardProps) {
  return (
    <div className="rounded-xl border border-border bg-bg-card p-5 transition-colors hover:border-accent-primary/30">
      <div className="mb-2 flex items-center gap-3">
        <h4 className="text-lg font-semibold text-accent-primary">
          "{expression.phrase}"
        </h4>
        {expression.audioUrl && (
          <AudioPlayer
            src={expression.audioUrl}
            fallbackText={expression.phrase}
            size="sm"
          />
        )}
      </div>

      <p className="text-text-primary">{expression.meaning}</p>

      {expression.contextQuote && (
        <div className="mt-4 rounded-lg bg-bg-dark p-4">
          <p className="mb-1 text-xs font-medium uppercase tracking-wider text-text-secondary">
            Context from the show
          </p>
          <p className="text-sm italic text-text-secondary">"{expression.contextQuote}"</p>
        </div>
      )}

      {expression.usageNote && (
        <div className="mt-4 flex items-start gap-2 rounded-lg border border-yellow-800/30 bg-yellow-900/10 p-3">
          <svg
            className="mt-0.5 h-4 w-4 shrink-0 text-yellow-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <p className="text-sm text-yellow-200/80">{expression.usageNote}</p>
        </div>
      )}
    </div>
  );
}
