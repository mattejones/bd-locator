package xyz.mej.bdlocator.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private List<ScoredLocationDto> results;
    private int totalResults;
    private Double originLat;
    private Double originLng;
    private Double radiusKm;
}
