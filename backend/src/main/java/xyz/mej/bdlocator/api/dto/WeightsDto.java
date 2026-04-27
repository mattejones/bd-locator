package xyz.mej.bdlocator.api.dto;

import lombok.Data;

@Data
public class WeightsDto {
    private Double proximityWeight    = 0.4;
    private Double ratingWeight       = 0.35;
    private Double scaleWeight        = 0.25;
    private Double proximityHalfLifeKm = 2.5;
    private Double scaleFactor        = 5.0;
}
