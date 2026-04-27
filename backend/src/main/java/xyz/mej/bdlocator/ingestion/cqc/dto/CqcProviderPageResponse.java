package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcProviderPageResponse {
    private int total;
    private int page;
    private int totalPages;
    private String nextPageUri;
    private List<CqcProviderSummary> providers;
}
