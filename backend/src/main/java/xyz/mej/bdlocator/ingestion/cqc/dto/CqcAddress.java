package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcAddress {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String county;
    private String postalCode;
}
