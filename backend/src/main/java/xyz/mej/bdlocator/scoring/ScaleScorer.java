package xyz.mej.bdlocator.scoring;

/**
 * Smooth scale score derived from a provider's total location count.
 *
 * Formula: score = 1 - exp(-locationCount / scaleFactor)
 *
 * Properties:
 *   - Score = 0.0 at locationCount 0
 *   - Score ≈ 0.18 for a single-location provider (scaleFactor = 5)
 *   - Score ≈ 0.63 at locationCount = scaleFactor
 *   - Score approaches 1.0 asymptotically for large groups
 *
 * This avoids a hard threshold (e.g. "10+ locations = large") and
 * reflects the intuition that each additional location adds diminishing
 * marginal value as a partnership signal.
 */
public final class ScaleScorer {

    private ScaleScorer() {}

    public static double compute(long locationCount, double scaleFactor) {
        return 1.0 - Math.exp(-locationCount / scaleFactor);
    }
}
