'use client';

import { useState, useCallback } from 'react';
import dynamic from 'next/dynamic';
import { SearchPanel } from '@/components/SearchPanel';
import { ResultCard } from '@/components/ResultCard';
import { ScoredLocation } from '@/lib/types';

const MapView = dynamic(
  () => import('@/components/MapView').then(m => m.MapView),
  { ssr: false }
);

export function AppShell() {
  const [results, setResults] = useState<ScoredLocation[]>([]);
  const [origin, setOrigin] = useState<{ lat: number; lng: number } | null>(null);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleResults = useCallback(
    (newResults: ScoredLocation[], newOrigin: { lat: number; lng: number }) => {
      setResults(newResults);
      setOrigin(newOrigin);
      setSelectedId(null);
    },
    []
  );

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Left panel */}
      <div className="w-80 shrink-0 border-r border-gray-800 flex flex-col">
        <SearchPanel
          onResults={handleResults}
          isLoading={isLoading}
          setIsLoading={setIsLoading}
        />
      </div>

      {/* Map */}
      <div className="flex-1 relative">
        <MapView
          results={results}
          origin={origin}
          selectedId={selectedId}
          onSelectLocation={setSelectedId}
        />

        {results.length > 0 && (
          <div className="absolute top-4 right-4 w-72 max-h-[calc(100vh-2rem)] overflow-y-auto flex flex-col gap-2">
            <div className="bg-gray-900 bg-opacity-90 rounded-lg px-3 py-2 text-xs text-gray-400">
              {results.length} targets found
            </div>
            {results.map((r, i) => (
              <ResultCard
                key={r.locationId}
                result={r}
                rank={i + 1}
                isSelected={selectedId === r.locationId}
                onSelect={() => setSelectedId(r.locationId)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
