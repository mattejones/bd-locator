'use client';

import { useEffect, useState } from 'react';
import Map from 'react-map-gl/mapbox';
import { DeckGL } from '@deck.gl/react';
import { ScatterplotLayer } from '@deck.gl/layers';
import { ScoredLocation } from '@/lib/types';
import 'mapbox-gl/dist/mapbox-gl.css';

interface MapViewProps {
  results: ScoredLocation[];
  origin: { lat: number; lng: number } | null;
  selectedId: string | null;
  onSelectLocation: (id: string) => void;
}

const MAPBOX_TOKEN = process.env.NEXT_PUBLIC_MAPBOX_TOKEN ?? '';

const INITIAL_VIEW = {
  longitude: -0.595,
  latitude: 51.510,
  zoom: 11,
  pitch: 0,
  bearing: 0,
};

export function MapView({ results, origin, selectedId, onSelectLocation }: MapViewProps) {
  const [viewState, setViewState] = useState(INITIAL_VIEW);

  // Re-centre when results change
  useEffect(() => {
    if (origin) {
      setViewState(v => ({ ...v, longitude: origin.lng, latitude: origin.lat, zoom: 11 }));
    }
  }, [origin]);

  const layers = [
    // Results layer
    new ScatterplotLayer<ScoredLocation>({
      id: 'results',
      data: results,
      getPosition: d => [d.lng ?? 0, d.lat ?? 0],
      getRadius: d => (d.locationId === selectedId ? 120 : 80),
      getFillColor: d => {
        if (d.locationId === selectedId) return [99, 102, 241, 255];   // indigo
        const s = d.score;
        if (s >= 0.7) return [52, 211, 153, 220];   // emerald — high score
        if (s >= 0.45) return [251, 191, 36, 220];  // amber — mid score
        return [239, 68, 68, 200];                  // red — low score
      },
      radiusUnits: 'meters',
      pickable: true,
      onClick: ({ object }) => object && onSelectLocation(object.locationId),
      updateTriggers: { getFillColor: selectedId, getRadius: selectedId },
    }),
    // Origin marker
    ...(origin ? [new ScatterplotLayer({
      id: 'origin',
      data: [origin],
      getPosition: (d: { lat: number; lng: number }) => [d.lng, d.lat],
      getRadius: 60,
      getFillColor: [255, 255, 255, 255],
      radiusUnits: 'meters',
      pickable: false,
    })] : []),
  ];

  return (
    <div className="w-full h-full relative">
      <DeckGL
        viewState={viewState}
        onViewStateChange={({ viewState: vs }) => setViewState(vs as typeof INITIAL_VIEW)}
        controller
        layers={layers}
      >
        <Map
          mapboxAccessToken={MAPBOX_TOKEN}
          mapStyle="mapbox://styles/mapbox/dark-v11"
        />
      </DeckGL>

      {/* Legend */}
      <div className="absolute bottom-6 right-4 bg-gray-900 bg-opacity-90 rounded-lg p-3 text-xs text-gray-300 flex flex-col gap-1.5">
        {[
          { colour: 'bg-emerald-400', label: 'Score ≥ 0.7' },
          { colour: 'bg-amber-400',   label: 'Score 0.45–0.7' },
          { colour: 'bg-red-400',     label: 'Score < 0.45' },
          { colour: 'bg-white',       label: 'Origin' },
        ].map(({ colour, label }) => (
          <div key={label} className="flex items-center gap-2">
            <div className={`w-2.5 h-2.5 rounded-full ${colour}`} />
            <span>{label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
