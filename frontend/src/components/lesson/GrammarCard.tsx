import type { GrammarPoint } from '../../types/lesson';

interface GrammarCardProps {
  grammarPoint: GrammarPoint;
}

export function GrammarCard({ grammarPoint }: GrammarCardProps) {
  return (
    <div className="rounded-xl border border-edge-default bg-bg-card p-5 shadow-sm transition-colors hover:border-brand/30">
      <h4 className="mb-3 text-lg font-semibold text-content-primary">{grammarPoint.title}</h4>

      <p className="text-content-secondary">{grammarPoint.explanation}</p>

      {grammarPoint.structure && (
        <div className="mt-4">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-content-secondary">
            Structure
          </p>
          <div className="rounded-lg bg-bg-inset p-3 font-mono text-sm text-brand">
            {grammarPoint.structure}
          </div>
        </div>
      )}

      {grammarPoint.example && (
        <div className="mt-4">
          <p className="mb-2 text-xs font-medium uppercase tracking-wider text-content-secondary">
            Example
          </p>
          <p className="rounded-lg bg-bg-inset p-3 text-sm italic text-content-primary">
            "{grammarPoint.example}"
          </p>
        </div>
      )}

      {grammarPoint.contextQuote && (
        <div className="mt-4 border-l-2 border-brand pl-4">
          <p className="text-sm text-content-secondary">
            <span className="font-medium text-brand">From the show:</span>{' '}
            "{grammarPoint.contextQuote}"
          </p>
        </div>
      )}
    </div>
  );
}
