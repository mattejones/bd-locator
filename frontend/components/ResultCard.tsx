'use client';

import { ScoredLocation } from '@/lib/types';

interface ResultCardProps {
  result: ScoredLocation;
  rank: number;
  isSelected: boolean;
  onSelect: () => void;
}

const RATING_COLOURS: Record<string, string> = {
  Outstanding: 'text-emerald-400',
  Good: 'text-green-400',
  'Requires Improvement': 'text-amber-400',
  Inadequate: 'text-red-400',
};

function ScoreBar({ value, colour }: { value: number; colour: string }) {
  return (
    <div className="h-1 w-full bg-gray-700 rounded-full overflow-hidden">
      <div
        className={`h-full rounded-full ${colour}`}
        style={{ width: `${Math.round(value * 100)}%` }}
      />
    </div>
  );
}

export function ResultCard({ result, rank, isSelected, onSelect }: ResultCardProps) {
  const ratingColour = result.rating ? RATING_COLOURS[result.rating] ?? 'text-gray-400' : 'text-gray-500';
  const distanceKm = (result.distanceMetres / 1000).toFixed(1);

  return (
    <div
      onClick={onSelect}
      className={`p-4 rounded-lg border cursor-pointer transition-colors ${
        isSelected
          ? 'border-indigo-500 bg-indigo-950'
          : 'border-gray-700 bg-gray-800 hover:border-gray-600'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-start gap-2 min-w-0">
          <span className="text-xs text-gray-500 font-mono mt-0.5 shrink-0">#{rank}</span>
          <div className="min-w-0">
            <p className="text-sm font-medium text-white truncate">{result.locationName}</p>
            <p className="text-xs text-gray-400 truncate">{result.providerName}</p>
          </div>
        </div>
        <span className="text-sm font-mono font-semibold text-indigo-400 shrink-0">
          {(result.score * 100).toFixed(0)}
        </span>
      </div>

      <div className="mt-3 flex items-center gap-4 text-xs text-gray-400">
        <span className={ratingColour}>{result.rating ?? 'Not rated'}</span>
        <span>{distanceKm} km</span>
        <span>{result.providerLocationCount} location{result.providerLocationCount !== 1 ? 's' : ''}</span>
      </div>

      {/* Score breakdown bars */}
      <div className="mt-3 flex flex-col gap-1.5">
        {[
          { label: 'Proximity', value: result.scoreBreakdown.proximity, colour: 'bg-indigo-500' },
          { label: 'Rating',    value: result.scoreBreakdown.rating,    colour: 'bg-emerald-500' },
          { label: 'Scale',     value: result.scoreBreakdown.scale,     colour: 'bg-amber-500' },
        ].map(({ label, value, colour }) => (
          <div key={label} className="flex items-center gap-2">
            <span className="text-xs text-gray-500 w-14 shrink-0">{label}</span>
            <ScoreBar value={value} colour={colour} />
          </div>
        ))}
      </div>
    </div>
  );
}
