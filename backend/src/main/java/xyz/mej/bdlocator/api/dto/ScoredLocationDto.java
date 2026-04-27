package xyz.mej.bdlocator.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScoredLocationDto {
    private String locationId;
    private String locationName;
    private String providerName;
    private String providerId;
    private String postcode;
    private String address;
    private String rating;
    private String registrationStatus;
    private List<String> serviceTypes;
    private List<String> userBands;
    private Double lat;
    private Double lng;
    private Double distanceMetres;
    private Double score;
    private ScoreBreakdownDto scoreBreakdown;
    private Long providerLocationCount;

    @Data
    @Builder
    public static class ScoreBreakdownDto {
        private Double proximity;
        private Double rating;
        private Double scale;
    }
}
