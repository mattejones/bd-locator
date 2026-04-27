package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcRating {
    private CqcRatingValue overall;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CqcRatingValue {
        private String rating;
    }
}
