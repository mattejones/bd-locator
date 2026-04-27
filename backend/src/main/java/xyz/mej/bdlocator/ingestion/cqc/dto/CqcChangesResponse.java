package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcChangesResponse {
    private List<CqcChangeEntry> changes;
    private String endTimestamp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CqcChangeEntry {
        private String providerId;
        private String locationId;
        private String type; // "provider" or "location"
    }
}
