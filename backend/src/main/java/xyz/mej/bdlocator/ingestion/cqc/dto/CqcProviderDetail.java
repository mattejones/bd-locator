package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcProviderDetail {
    private String providerId;
    private String name;
    private String companiesHouseNumber;
    private String type;               // organisation type e.g. "Social Care Org"
    private String postalCode;
    private CqcAddress address;
    private List<CqcLocation> locations;
}
