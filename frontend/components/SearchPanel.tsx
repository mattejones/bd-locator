'use client';

import { useState } from 'react';
import { Weights, ScoredLocation } from '@/lib/types';

interface SearchPanelProps {
  onResults: (results: ScoredLocation[], origin: { lat: number; lng: number }) => void;
  isLoading: boolean;
  setIsLoading: (v: boolean) => void;
}

const DIMENSIONS = [
  {
    key: 'proximity' as const,
    label: 'Distance',
    hint: 'How close the provider is to your location',
  },
  {
    key: 'rating' as const,
    label: 'Quality',
    hint: 'CQC inspection rating',
  },
  {
    key: 'scale' as const,
    label: 'Group size',
    hint: 'Number of sites the provider operates',
  },
];

const DEFAULT_SCORES = { proximity: 8, rating: 7, scale: 5 };

function WeightSlider({
  label,
  hint,
  value,
  onChange,
}: {
  label: string;
  hint: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex justify-between items-baseline">
        <div>
          <span className="text-sm text-gray-300">{label}</span>
          <p className="text-xs text-gray-500 mt-0.5">{hint}</p>
        </div>
        <span className="text-xs font-mono text-indigo-400 ml-2 shrink-0">{value}/10</span>
      </div>
      <div className="flex items-center gap-2">
        <span className="text-xs text-gray-600">Less</span>
        <input
          type="range"
          min={1}
          max={10}
          step={1}
          value={value}
          onChange={e => onChange(Number(e.target.value))}
          className="flex-1 accent-indigo-500"
        />
        <span className="text-xs text-gray-600">More</span>
      </div>
    </div>
  );
}

async function geocodePostcode(postcode: string): Promise<{ lat: number; lng: number } | null> {
  try {
    const res = await fetch(
      `https://api.postcodes.io/postcodes/${encodeURIComponent(postcode.trim())}`
    );
    const data = await res.json();
    if (data.status === 200 && data.result) {
      return { lat: data.result.latitude, lng: data.result.longitude };
    }
    return null;
  } catch {
    return null;
  }
}

function resolveWeights(scores: typeof DEFAULT_SCORES): Weights {
  const total = scores.proximity + scores.rating + scores.scale;
  return {
    proximityWeight: scores.proximity / total,
    ratingWeight: scores.rating / total,
    scaleWeight: scores.scale / total,
    proximityHalfLifeKm: 2.5,
    scaleFactor: 5.0,
  };
}

export function SearchPanel({ onResults, isLoading, setIsLoading }: SearchPanelProps) {
  const [postcode, setPostcode] = useState('SG6 1GJ');
  const [postcodeError, setPostcodeError] = useState<string | null>(null);
  const [icpDescription, setIcpDescription] = useState('');
  const [radiusKm, setRadiusKm] = useState(10);
  const [scores, setScores] = useState(DEFAULT_SCORES);

  const setScore = (key: keyof typeof DEFAULT_SCORES, value: number) => {
    setScores(prev => ({ ...prev, [key]: value }));
  };

  const handleSearch = async () => {
    setPostcodeError(null);
    setIsLoading(true);
    try {
      const origin = await geocodePostcode(postcode);
      if (!origin) {
        setPostcodeError('Postcode not found — check and try again');
        return;
      }
      const { search } = await import('@/lib/api');
      const data = await search({
        lat: origin.lat,
        lng: origin.lng,
        radiusKm,
        weights: resolveWeights(scores),
        icpDescription: icpDescription.trim() || undefined,
      });
      onResults(data.results, origin);
    } catch (err) {
      console.error('Search error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-5 p-5 bg-gray-900 text-white h-full overflow-y-auto">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">BD Locator</h1>
        <p className="text-gray-400 text-sm mt-1">Care sector targeting tool</p>
      </div>

      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-medium text-gray-300">Your postcode</label>
        <input
          type="text"
          className={`bg-gray-800 border rounded-lg p-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500 transition-colors ${
            postcodeError ? 'border-red-500' : 'border-gray-700'
          }`}
          placeholder="e.g. SG6 1GJ"
          value={postcode}
          onChange={e => { setPostcode(e.target.value); setPostcodeError(null); }}
        />
        {postcodeError && <p className="text-xs text-red-400">{postcodeError}</p>}
      </div>

      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-medium text-gray-300">Ideal partner</label>
        <textarea
          className="bg-gray-800 border border-gray-700 rounded-lg p-2.5 text-sm text-white placeholder-gray-500 resize-none focus:outline-none focus:border-indigo-500 transition-colors"
          rows={3}
          placeholder="e.g. memory care homes for older adults run by larger groups"
          value={icpDescription}
          onChange={e => setIcpDescription(e.target.value)}
        />
        <p className="text-xs text-gray-500">AI expands this into CQC category filters</p>
      </div>

      <div className="flex flex-col gap-1.5">
        <div className="flex justify-between items-center">
          <label className="text-sm font-medium text-gray-300">Search radius</label>
          <span className="text-sm text-indigo-400 font-mono">{radiusKm} km</span>
        </div>
        <input
          type="range" min={1} max={25} step={1}
          value={radiusKm}
          onChange={e => setRadiusKm(Number(e.target.value))}
          className="accent-indigo-500"
        />
      </div>

      <div className="flex flex-col gap-4">
        <label className="text-sm font-medium text-gray-300">What matters most?</label>
        {DIMENSIONS.map(({ key, label, hint }) => (
          <WeightSlider
            key={key}
            label={label}
            hint={hint}
            value={scores[key]}
            onChange={v => setScore(key, v)}
          />
        ))}
      </div>

      <button
        onClick={handleSearch}
        disabled={isLoading || !postcode.trim()}
        className="mt-auto bg-indigo-600 hover:bg-indigo-500 disabled:bg-gray-700 disabled:text-gray-500 text-white font-medium py-2.5 rounded-lg transition-colors text-sm"
      >
        {isLoading ? 'Searching...' : 'Find targets'}
      </button>
    </div>
  );
}
