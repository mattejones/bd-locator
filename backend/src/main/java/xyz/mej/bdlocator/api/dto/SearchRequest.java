package xyz.mej.bdlocator.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SearchRequest {

    @NotNull
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double lat;

    @NotNull
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double lng;

    private Double radiusKm = 5.0;

    private WeightsDto weights;

    /**
     * Natural language ICP description — passed to LLM expansion in Phase 5.
     * Ignored for now; included in the contract so the frontend can start sending it.
     */
    private String icpDescription;
}
