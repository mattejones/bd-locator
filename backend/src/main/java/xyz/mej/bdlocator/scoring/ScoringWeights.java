package xyz.mej.bdlocator.scoring;

import lombok.Builder;
import lombok.Data;

/**
 * Configurable weights for the scoring model.
 * All weights must be positive and should sum to 1.0.
 * Validation is the caller's responsibility.
 */
@Data
@Builder
public class ScoringWeights {

    @Builder.Default
    private double proximityWeight = 0.4;

    @Builder.Default
    private double ratingWeight = 0.35;

    @Builder.Default
    private double scaleWeight = 0.25;

    /**
     * Half-life distance in km for the proximity decay function.
     * At this distance, the proximity score is ~0.37.
     * Default: 2.5km — a natural neighbourhood unit for a BD on foot or by car.
     */
    @Builder.Default
    private double proximityHalfLifeKm = 2.5;

    /**
     * Scale factor for the provider scale sigmoid.
     * Controls how quickly the scale score rises with location count.
     * Default: 5 — a provider with 5 locations scores ~0.63.
     */
    @Builder.Default
    private double scaleFactor = 5.0;

    public static ScoringWeights defaults() {
        return ScoringWeights.builder().build();
    }
}
