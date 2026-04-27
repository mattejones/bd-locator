package xyz.mej.bdlocator.scoring;

import xyz.mej.bdlocator.domain.model.Location;
import lombok.Builder;
import lombok.Data;

/**
 * A scored location result — carries both the composite score
 * and the individual dimension breakdown for UI transparency.
 */
@Data
@Builder
public class ScoredLocation {

    private Location location;

    /** Distance from origin in metres */
    private double distanceMetres;

    /** Composite weighted score — 0.0 to 1.0 */
    private double score;

    /** Individual dimension scores — all 0.0 to 1.0 */
    private double proximityScore;
    private double ratingScore;
    private double scaleScore;

    /** Number of locations under the same provider — raw scale signal */
    private long providerLocationCount;
}
