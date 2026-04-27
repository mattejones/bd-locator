package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcLocationDetail {
    private String locationId;
    private String providerId;
    private String name;
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
    private String careHome;            // "Y" or "N"
    private String inspectionDirectorate;
    private Double onspdLatitude;
    private Double onspdLongitude;

    private CqcRating currentRatings;
    private List<CqcServiceType> gacServiceTypes;

    // specialisms maps to user bands in the actual API schema
    private List<CqcSpecialism> specialisms;
}
