package xyz.mej.bdlocator.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import xyz.mej.bdlocator.api.dto.*;
import xyz.mej.bdlocator.llm.IcpExpansionService;
import xyz.mej.bdlocator.scoring.ScoredLocation;
import xyz.mej.bdlocator.scoring.ScoringService;
import xyz.mej.bdlocator.scoring.ScoringWeights;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final ScoringService scoringService;
    private final IcpExpansionService icpExpansionService;

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        ScoringWeights weights = resolveWeights(request.getWeights());
        Set<String> icpKeywords = icpExpansionService.expand(request.getIcpDescription());

        List<ScoredLocation> results = scoringService.score(
                request.getLat(),
                request.getLng(),
                request.getRadiusKm() != null ? request.getRadiusKm() : 5.0,
                weights,
                icpKeywords
        );

        List<ScoredLocationDto> dtos = results.stream()
                .map(this::toDto)
                .toList();

        return SearchResponse.builder()
                .results(dtos)
                .totalResults(dtos.size())
                .originLat(request.getLat())
                .originLng(request.getLng())
                .radiusKm(request.getRadiusKm())
                .build();
    }

    private ScoringWeights resolveWeights(WeightsDto dto) {
        if (dto == null) return ScoringWeights.defaults();
        return ScoringWeights.builder()
                .proximityWeight(dto.getProximityWeight())
                .ratingWeight(dto.getRatingWeight())
                .scaleWeight(dto.getScaleWeight())
                .proximityHalfLifeKm(dto.getProximityHalfLifeKm())
                .scaleFactor(dto.getScaleFactor())
                .build();
    }

    private ScoredLocationDto toDto(ScoredLocation scored) {
        var location = scored.getLocation();
        var provider = location.getProvider();

        Double lat = null;
        Double lng = null;
        if (location.getCoordinates() != null) {
            lat = location.getCoordinates().getY();
            lng = location.getCoordinates().getX();
        }

        return ScoredLocationDto.builder()
                .locationId(location.getLocationId())
                .locationName(location.getLocationName())
                .providerName(provider != null ? provider.getProviderName() : null)
                .providerId(provider != null ? provider.getProviderId() : null)
                .postcode(location.getPostcode())
                .address(location.getAddressLines())
                .rating(location.getRating())
                .registrationStatus(location.getRegistrationStatus())
                .serviceTypes(location.getServiceTypes() != null
                        ? Arrays.asList(location.getServiceTypes()) : List.of())
                .userBands(location.getUserBands() != null
                        ? Arrays.asList(location.getUserBands()) : List.of())
                .lat(lat)
                .lng(lng)
                .distanceMetres(scored.getDistanceMetres())
                .score(scored.getScore())
                .scoreBreakdown(ScoredLocationDto.ScoreBreakdownDto.builder()
                        .proximity(scored.getProximityScore())
                        .rating(scored.getRatingScore())
                        .scale(scored.getScaleScore())
                        .build())
                .providerLocationCount(scored.getProviderLocationCount())
                .build();
    }
}
