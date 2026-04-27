package xyz.mej.bdlocator.ingestion.geocoding;

import xyz.mej.bdlocator.ingestion.geocoding.dto.PostcodesIoResponse;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class GeocodingService {

    private final WebClient webClient;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeocodingService(
            WebClient.Builder builder,
            @Value("${app.postcodes.base-url}") String baseUrl
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Resolves a UK postcode to a PostGIS Point.
     * Returns empty if the postcode is invalid, not found, or the API is unavailable.
     * Callers must handle empty gracefully — a null geometry is not a pipeline failure.
     */
    public Optional<Point> geocode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return Optional.empty();
        }

        try {
            PostcodesIoResponse response = webClient.get()
                    .uri("/postcodes/{postcode}", postcode.trim().replace(" ", ""))
                    .retrieve()
                    .bodyToMono(PostcodesIoResponse.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();

            if (response == null || response.getResult() == null) {
                log.warn("No geocoding result for postcode: {}", postcode);
                return Optional.empty();
            }

            Point point = geometryFactory.createPoint(
                    new Coordinate(response.getResult().getLongitude(), response.getResult().getLatitude())
            );
            return Optional.of(point);

        } catch (Exception e) {
            log.warn("Geocoding failed for postcode {}: {}", postcode, e.getMessage());
            return Optional.empty();
        }
    }
}
