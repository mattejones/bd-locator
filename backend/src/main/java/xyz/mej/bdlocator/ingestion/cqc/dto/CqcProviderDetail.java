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
    private String type;
    private String organisationType;
    private String registrationStatus;
    private String region;
    private String localAuthority;
    private String postalCode;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private String postalAddressTownCity;
    private String postalAddressCounty;
    private String website;
    private String mainPhoneNumber;
    private Double onspdLatitude;
    private Double onspdLongitude;

    // Array of plain location ID strings per the actual API schema
    private List<String> locationIds;
}
