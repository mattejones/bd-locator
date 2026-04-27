'use client';

import { useState } from 'react';
import { Weights, ScoredLocation } from '@/lib/types';

interface SearchPanelProps {
  onResults: (results: ScoredLocation[], origin: { lat: number; lng: number }) => void;
  isLoading: boolean;
  setIsLoading: (v: boolean) => void;
}

const DEFAULT_WEIGHTS: Weights = {
  proximityWeight: 0.4,
  ratingWeight: 0.35,
  scaleWeight: 0.25,
  proximityHalfLifeKm: 2.5,
  scaleFactor: 5.0,
};

const HERITAGE_LOUNGE = { lat: 51.5105, lng: -0.595 };

export function SearchPanel({ onResults, isLoading, setIsLoading }: SearchPanelProps) {
  const [icpDescription, setIcpDescription] = useState('');
  const [radiusKm, setRadiusKm] = useState(10);
  const [weights, setWeights] = useState<Weights>(DEFAULT_WEIGHTS);

  const handleSearch = async () => {
    setIsLoading(true);
    try {
      const { search } = await import('@/lib/api');
      const data = await search({
        lat: HERITAGE_LOUNGE.lat,
        lng: HERITAGE_LOUNGE.lng,
        radiusKm,
        weights,
        icpDescription: icpDescription.trim() || undefined,
      });
      onResults(data.results, HERITAGE_LOUNGE);
    } catch (err) {
      console.error('Search error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const setWeight = (key: keyof Weights, value: number) => {
    setWeights(prev => ({ ...prev, [key]: value }));
  };

  return (
    <div className="flex flex-col gap-6 p-6 bg-gray-900 text-white h-full overflow-y-auto">
      <div>
        <h1 className="text-xl font-semibold tracking-tight">BD Locator</h1>
        <p className="text-gray-400 text-sm mt-1">Care sector targeting tool</p>
      </div>

      {/* ICP description */}
      <div className="flex flex-col gap-2">
        <label className="text-sm font-medium text-gray-300">Ideal partner description</label>
        <textarea
          className="bg-gray-800 border border-gray-700 rounded-lg p-3 text-sm text-white placeholder-gray-500 resize-none focus:outline-none focus:border-indigo-500 transition-colors"
          rows={3}
          placeholder="e.g. memory care homes for older adults run by larger groups"
          value={icpDescription}
          onChange={e => setIcpDescription(e.target.value)}
        />
        <p className="text-xs text-gray-500">Expanded by AI into CQC category filters</p>
      </div>

      {/* Radius */}
      <div className="flex flex-col gap-2">
        <div className="flex justify-between items-center">
          <label className="text-sm font-medium text-gray-300">Radius</label>
          <span className="text-sm text-indigo-400 font-mono">{radiusKm} km</span>
        </div>
        <input
          type="range" min={1} max={25} step={1}
          value={radiusKm}
          onChange={e => setRadiusKm(Number(e.target.value))}
          className="accent-indigo-500"
        />
      </div>

      {/* Weight sliders */}
      <div className="flex flex-col gap-4">
        <label className="text-sm font-medium text-gray-300">Scoring weights</label>
        {[
          { key: 'proximityWeight' as const, label: 'Proximity' },
          { key: 'ratingWeight' as const, label: 'CQC rating' },
          { key: 'scaleWeight' as const, label: 'Provider scale' },
        ].map(({ key, label }) => (
          <div key={key} className="flex flex-col gap-1">
            <div className="flex justify-between">
              <span className="text-xs text-gray-400">{label}</span>
              <span className="text-xs text-indigo-400 font-mono">
                {weights[key].toFixed(2)}
              </span>
            </div>
            <input
              type="range" min={0} max={1} step={0.05}
              value={weights[key]}
              onChange={e => setWeight(key, Number(e.target.value))}
              className="accent-indigo-500"
            />
          </div>
        ))}
      </div>

      <button
        onClick={handleSearch}
        disabled={isLoading}
        className="mt-auto bg-indigo-600 hover:bg-indigo-500 disabled:bg-gray-700 disabled:text-gray-500 text-white font-medium py-3 rounded-lg transition-colors text-sm"
      >
        {isLoading ? 'Searching...' : 'Find targets'}
      </button>
    </div>
  );
}
