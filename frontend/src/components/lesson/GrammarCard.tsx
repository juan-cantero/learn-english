import type { GrammarPoint } from '../../types/lesson';

interface GrammarCardProps {
  grammarPoint: GrammarPoint;
}

export function GrammarCard({ grammarPoint }: GrammarCardProps) {
  return (
    <div className="rounded-xl border border-border bg-bg-card p-5 transition-colors hover:border-accent-primary/30">
      <h4 className="mb-3 text-lg font-semibold text-text-primary">{grammarPoint.title}</h4>

      <p className="text-text-secondary">{grammarPoint.explanation}</p>

      {grammarPoint.structure && (
        <div className="mt-4">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-text-secondary">
            Structure
          </p>
          <div className="rounded-lg bg-bg-dark p-3 font-mono text-sm text-accent-primary">
            {grammarPoint.structure}
          </div>
        </div>
      )}

      {grammarPoint.example && (
        <div className="mt-4">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-text-secondary">
            Example
          </p>
          <p className="rounded-lg bg-bg-dark p-3 text-sm italic text-text-primary">
            "{grammarPoint.example}"
          </p>
        </div>
      )}

      {grammarPoint.contextQuote && (
        <div className="mt-4 border-l-2 border-accent-primary pl-4">
          <p className="text-sm text-text-secondary">
            <span className="font-medium text-accent-primary">From the show:</span>{' '}
            "{grammarPoint.contextQuote}"
          </p>
        </div>
      )}
    </div>
  );
}
