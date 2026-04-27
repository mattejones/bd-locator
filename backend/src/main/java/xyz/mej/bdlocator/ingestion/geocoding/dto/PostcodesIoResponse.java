package xyz.mej.bdlocator.ingestion.geocoding.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostcodesIoResponse {
    private int status;
    private PostcodeResult result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostcodeResult {
        private double latitude;
        private double longitude;
    }
}
