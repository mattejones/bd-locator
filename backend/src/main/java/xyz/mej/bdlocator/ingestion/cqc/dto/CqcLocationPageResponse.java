package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcLocationPageResponse {
    private int total;
    private int page;
    private int totalPages;
    private String nextPageUri;
    private List<CqcLocationSummary> locations;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CqcLocationSummary {
        private String locationId;
        private String locationName;
        private String providerId;
    }
}
