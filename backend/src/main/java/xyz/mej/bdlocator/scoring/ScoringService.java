package xyz.mej.bdlocator.scoring;

import xyz.mej.bdlocator.domain.model.Location;
import xyz.mej.bdlocator.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final LocationRepository locationRepository;

    @Value("${app.scoring.default-radius-km:5.0}")
    private double defaultRadiusKm;

    @Value("${app.scoring.max-radius-km:25.0}")
    private double maxRadiusKm;

    /**
     * Core scoring entry point.
     *
     * @param lat         Origin latitude (Heritage Lounge location)
     * @param lng         Origin longitude
     * @param radiusKm    Search radius — clamped to maxRadiusKm
     * @param weights     Configurable dimension weights
     * @param icpKeywords Expanded ICP keywords from LLM — acts as a gate,
     *                    not a scoring dimension. Null or empty = no gate applied.
     * @return Scored locations sorted by composite score descending
     */
    public List<ScoredLocation> score(
            double lat,
            double lng,
            double radiusKm,
            ScoringWeights weights,
            Set<String> icpKeywords
    ) {
        double clampedRadius = Math.min(radiusKm, maxRadiusKm);
        double radiusMetres = clampedRadius * 1000.0;

        List<Location> candidates = locationRepository.findWithinRadius(lat, lng, radiusMetres);
        log.debug("Found {} candidate locations within {}km of ({}, {})",
                candidates.size(), clampedRadius, lat, lng);

        return candidates.stream()
                .filter(loc -> passesIcpGate(loc, icpKeywords))
                .map(loc -> scoreLocation(loc, lat, lng, weights))
                .sorted(Comparator.comparingDouble(ScoredLocation::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * ICP gate — returns false if icpKeywords is non-empty and the location's
     * service types share no overlap with the keyword set.
     * Case-insensitive substring matching to handle partial CQC taxonomy terms.
     */
    private boolean passesIcpGate(Location location, Set<String> icpKeywords) {
        if (icpKeywords == null || icpKeywords.isEmpty()) {
            return true;
        }
        if (location.getServiceTypes() == null || location.getServiceTypes().length == 0) {
            return false;
        }

        Set<String> lowerKeywords = icpKeywords.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return Arrays.stream(location.getServiceTypes())
                .anyMatch(st -> lowerKeywords.stream()
                        .anyMatch(kw -> st.toLowerCase().contains(kw)));
    }

    private ScoredLocation scoreLocation(
            Location location,
            double originLat,
            double originLng,
            ScoringWeights weights
    ) {
        Double distanceMetres = locationRepository.distanceMetresTo(
                location.getLocationId(), originLat, originLng
        );
        if (distanceMetres == null) distanceMetres = 0.0;

        long providerLocationCount = location.getProvider() != null
                ? locationRepository.countByProviderProviderId(location.getProvider().getProviderId())
                : 1L;

        double proximityScore = ProximityDecay.compute(distanceMetres, weights.getProximityHalfLifeKm());
        double ratingScore    = RatingNormaliser.normalise(location.getRating());
        double scaleScore     = ScaleScorer.compute(providerLocationCount, weights.getScaleFactor());

        double compositeScore =
                weights.getProximityWeight() * proximityScore +
                weights.getRatingWeight()    * ratingScore    +
                weights.getScaleWeight()     * scaleScore;

        return ScoredLocation.builder()
                .location(location)
                .distanceMetres(distanceMetres)
                .proximityScore(proximityScore)
                .ratingScore(ratingScore)
                .scaleScore(scaleScore)
                .score(compositeScore)
                .providerLocationCount(providerLocationCount)
                .build();
    }
}
