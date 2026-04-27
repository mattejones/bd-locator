package xyz.mej.bdlocator.scoring;

/**
 * Maps CQC overall rating strings to a normalised 0–1 score.
 *
 * Unrated locations receive a neutral 0.5 rather than a penalty —
 * a new or recently re-registered provider should not be deprioritised
 * simply because CQC has not yet inspected them.
 */
public final class RatingNormaliser {

    private RatingNormaliser() {}

    public static double normalise(String rating) {
        if (rating == null || rating.isBlank()) {
            return 0.5;
        }
        return switch (rating.trim().toLowerCase()) {
            case "outstanding"           -> 1.0;
            case "good"                  -> 0.75;
            case "requires improvement"  -> 0.4;
            case "inadequate"            -> 0.1;
            default                      -> 0.5;
        };
    }
}
