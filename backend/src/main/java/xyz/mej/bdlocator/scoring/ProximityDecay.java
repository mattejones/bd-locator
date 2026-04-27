package xyz.mej.bdlocator.scoring;

/**
 * Smooth exponential decay function for proximity scoring.
 *
 * Formula: score = exp(-distanceKm / halfLifeKm)
 *
 * Properties:
 *   - Score = 1.0 at distance 0
 *   - Score ≈ 0.37 at distance = halfLifeKm
 *   - Score ≈ 0.14 at distance = 2x halfLifeKm
 *   - Score approaches 0 asymptotically — no hard cliff edge
 *
 * This is preferable to a linear cutoff because it reflects how a BD
 * actually thinks about proximity: nearby is strongly preferred, but
 * a high-quality provider slightly outside the core zone shouldn't
 * disappear entirely from the results.
 */
public final class ProximityDecay {

    private ProximityDecay() {}

    public static double compute(double distanceMetres, double halfLifeKm) {
        double distanceKm = distanceMetres / 1000.0;
        return Math.exp(-distanceKm / halfLifeKm);
    }
}
