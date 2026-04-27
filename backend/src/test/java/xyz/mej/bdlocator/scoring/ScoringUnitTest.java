package xyz.mej.bdlocator.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ScoringUnitTest {

    @Test
    void proximityDecay_atOrigin_isOne() {
        assertThat(ProximityDecay.compute(0, 2.5)).isCloseTo(1.0, within(0.001));
    }

    @Test
    void proximityDecay_atHalfLife_isApprox037() {
        // exp(-1) ≈ 0.368
        assertThat(ProximityDecay.compute(2500, 2.5)).isCloseTo(0.368, within(0.01));
    }

    @Test
    void proximityDecay_decreasesWithDistance() {
        double near = ProximityDecay.compute(500, 2.5);
        double far  = ProximityDecay.compute(5000, 2.5);
        assertThat(near).isGreaterThan(far);
    }

    @Test
    void ratingNormaliser_outstanding_isOne() {
        assertThat(RatingNormaliser.normalise("Outstanding")).isEqualTo(1.0);
    }

    @Test
    void ratingNormaliser_null_isNeutral() {
        assertThat(RatingNormaliser.normalise(null)).isEqualTo(0.5);
    }

    @Test
    void ratingNormaliser_caseInsensitive() {
        assertThat(RatingNormaliser.normalise("GOOD")).isEqualTo(0.75);
        assertThat(RatingNormaliser.normalise("requires improvement")).isEqualTo(0.4);
    }

    @Test
    void scaleScorer_singleLocation_isLow() {
        double score = ScaleScorer.compute(1, 5.0);
        assertThat(score).isLessThan(0.25);
    }

    @Test
    void scaleScorer_atScaleFactor_isApprox063() {
        // 1 - exp(-1) ≈ 0.632
        assertThat(ScaleScorer.compute(5, 5.0)).isCloseTo(0.632, within(0.01));
    }

    @Test
    void scaleScorer_largeGroup_approachesOne() {
        double score = ScaleScorer.compute(100, 5.0);
        assertThat(score).isGreaterThan(0.99);
    }

    @Test
    void weights_defaultsAreValid() {
        ScoringWeights w = ScoringWeights.defaults();
        double sum = w.getProximityWeight() + w.getRatingWeight() + w.getScaleWeight();
        assertThat(sum).isCloseTo(1.0, within(0.001));
    }
}
