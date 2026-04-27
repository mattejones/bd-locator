package xyz.mej.bdlocator.ingestion.cqc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqcSpecialism {
    private String name;
}
