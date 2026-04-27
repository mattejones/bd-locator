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
    private String postalCode;
    private CqcAddress address;
    private String registrationStatus;
    private CqcRating currentRatings;
    private List<CqcServiceType> gacServiceTypes;
    private List<CqcUserBand> serviceUserBands;
    private int numberOfBeds;
}
