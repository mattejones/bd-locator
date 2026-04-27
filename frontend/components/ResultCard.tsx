'use client';

import { useEffect, useRef, useState } from 'react';
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
      <div className={`h-full rounded-full ${colour}`} style={{ width: `${Math.round(value * 100)}%` }} />
    </div>
  );
}

function ActionButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button
      onClick={e => { e.stopPropagation(); onClick(); }}
      className="text-xs text-gray-400 hover:text-white bg-gray-700 hover:bg-gray-600 px-2.5 py-1 rounded transition-colors"
    >
      {label}
    </button>
  );
}

export function ResultCard({ result, rank, isSelected, onSelect }: ResultCardProps) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [copied, setCopied] = useState(false);

  // Scroll into view when selected from the map
  useEffect(() => {
    if (isSelected && cardRef.current) {
      cardRef.current.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }, [isSelected]);

  const ratingColour = result.rating ? RATING_COLOURS[result.rating] ?? 'text-gray-400' : 'text-gray-500';
  const distanceKm = (result.distanceMetres / 1000).toFixed(1);

  const handleDirections = () => {
    if (result.lat && result.lng) {
      window.open(`https://www.google.com/maps/dir/?api=1&destination=${result.lat},${result.lng}`, '_blank');
    } else {
      window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${result.locationName} ${result.postcode}`)}`, '_blank');
    }
  };

  const handleCopyAddress = () => {
    const text = [result.locationName, result.address, result.postcode].filter(Boolean).join(', ');
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  };

  const handleCompaniesHouse = () => {
    const query = result.providerName ?? result.locationName;
    window.open(`https://find-and-update.company-information.service.gov.uk/search?q=${encodeURIComponent(query)}`, '_blank');
  };

  const handleWebSearch = () => {
    window.open(`https://www.google.com/search?q=${encodeURIComponent(`${result.locationName} ${result.postcode}`)}`, '_blank');
  };

  return (
    <div
      ref={cardRef}
      onClick={onSelect}
      className={`p-4 rounded-lg border cursor-pointer transition-all duration-200 ${
        isSelected
          ? 'border-indigo-500 bg-indigo-950 shadow-lg shadow-indigo-900/30 ring-1 ring-indigo-500/30'
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
        <span className={`text-sm font-mono font-semibold shrink-0 ${isSelected ? 'text-indigo-300' : 'text-indigo-400'}`}>
          {(result.score * 100).toFixed(0)}
        </span>
      </div>

      <div className="mt-2 flex items-center gap-3 text-xs text-gray-400">
        <span className={ratingColour}>{result.rating ?? 'Not rated'}</span>
        <span>{distanceKm} km away</span>
        <span>{result.providerLocationCount} site{result.providerLocationCount !== 1 ? 's' : ''}</span>
      </div>

      {result.postcode && (
        <p className="mt-1 text-xs text-gray-500">
          {result.address ? `${result.address}, ` : ''}{result.postcode}
        </p>
      )}

      <div className="mt-3 flex flex-col gap-1.5">
        {[
          { label: 'Distance', value: result.scoreBreakdown.proximity, colour: 'bg-indigo-500' },
          { label: 'Quality',  value: result.scoreBreakdown.rating,    colour: 'bg-emerald-500' },
          { label: 'Scale',    value: result.scoreBreakdown.scale,     colour: 'bg-amber-500' },
        ].map(({ label, value, colour }) => (
          <div key={label} className="flex items-center gap-2">
            <span className="text-xs text-gray-500 w-14 shrink-0">{label}</span>
            <ScoreBar value={value} colour={colour} />
          </div>
        ))}
      </div>

      <div className="mt-3 flex flex-wrap gap-1.5">
        <ActionButton label="Directions" onClick={handleDirections} />
        <ActionButton label={copied ? 'Copied!' : 'Copy address'} onClick={handleCopyAddress} />
        <ActionButton label="Companies House" onClick={handleCompaniesHouse} />
        <ActionButton label="Web search" onClick={handleWebSearch} />
      </div>
    </div>
  );
}
