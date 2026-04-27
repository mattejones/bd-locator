export interface ScoreBreakdown {
  proximity: number;
  rating: number;
  scale: number;
}

export interface ScoredLocation {
  locationId: string;
  locationName: string;
  providerName: string | null;
  providerId: string | null;
  postcode: string;
  address: string | null;
  rating: string | null;
  registrationStatus: string | null;
  serviceTypes: string[];
  userBands: string[];
  lat: number | null;
  lng: number | null;
  distanceMetres: number;
  score: number;
  scoreBreakdown: ScoreBreakdown;
  providerLocationCount: number;
}

export interface SearchResponse {
  results: ScoredLocation[];
  totalResults: number;
  originLat: number;
  originLng: number;
  radiusKm: number;
}

export interface Weights {
  proximityWeight: number;
  ratingWeight: number;
  scaleWeight: number;
  proximityHalfLifeKm: number;
  scaleFactor: number;
}

export interface SearchRequest {
  lat: number;
  lng: number;
  radiusKm: number;
  weights: Weights;
  icpDescription?: string;
}
