package xyz.mej.bdlocator.ingestion.cqc;

import xyz.mej.bdlocator.domain.model.Location;
import xyz.mej.bdlocator.domain.model.Provider;
import xyz.mej.bdlocator.domain.repository.LocationRepository;
import xyz.mej.bdlocator.domain.repository.ProviderRepository;
import xyz.mej.bdlocator.ingestion.IngestionWatermark;
import xyz.mej.bdlocator.ingestion.IngestionWatermarkRepository;
import xyz.mej.bdlocator.ingestion.cqc.dto.*;
import xyz.mej.bdlocator.ingestion.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CqcIngestionService {

    private final CqcClient cqcClient;
    private final GeocodingService geocodingService;
    private final ProviderRepository providerRepository;
    private final LocationRepository locationRepository;
    private final IngestionWatermarkRepository watermarkRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Value("${app.cqc.page-size:1000}")
    private int pageSize;

    private static final String WATERMARK_SOURCE = "cqc";

    public void runBootstrap() {
        log.info("Starting CQC full bootstrap");
        int page = 1;
        int totalPages;
        do {
            log.info("Fetching provider page {}", page);
            CqcProviderPageResponse response = cqcClient.fetchProviderPage(page, pageSize);
            totalPages = response.getTotalPages();
            response.getProviders().forEach(s ->
                    cqcClient.fetchProviderDetail(s.getProviderId()).ifPresent(this::ingestProvider));
            page++;
        } while (page <= totalPages);
        advanceWatermark(OffsetDateTime.now());
        log.info("CQC full bootstrap complete");
    }

    public void runRegionalBootstrap(String region) {
        log.info("Starting regional bootstrap for: {}", region);
        int page = 1;
        int totalPages;
        int count = 0;
        do {
            log.info("Fetching provider page {} for region: {}", page, region);
            CqcProviderPageResponse response = cqcClient.fetchProviderPageByRegion(page, pageSize, region);
            if (response == null || response.getProviders() == null) break;
            totalPages = response.getTotalPages();
            for (CqcProviderSummary summary : response.getProviders()) {
                cqcClient.fetchProviderDetail(summary.getProviderId()).ifPresent(this::ingestProvider);
                count++;
            }
            page++;
        } while (page <= totalPages);
        advanceWatermark(OffsetDateTime.now());
        log.info("Regional bootstrap complete — {} providers for region: {}", count, region);
    }

    public void runDeltaSync() {
        OffsetDateTime since = getWatermark();
        OffsetDateTime until = OffsetDateTime.now();
        log.info("Starting delta sync from {} to {}", since, until);
        CqcChangesResponse changes = cqcClient.fetchChanges(since, until);
        if (changes == null || changes.getChanges() == null) {
            log.info("No changes found");
            advanceWatermark(until);
            return;
        }
        changes.getChanges().forEach(change -> {
            if ("location".equals(change.getType()) && change.getLocationId() != null) {
                cqcClient.fetchLocationDetail(change.getLocationId()).ifPresent(this::ingestLocation);
            } else if ("provider".equals(change.getType()) && change.getProviderId() != null) {
                cqcClient.fetchProviderDetail(change.getProviderId()).ifPresent(this::ingestProvider);
            }
        });
        advanceWatermark(until);
        log.info("Delta sync complete — {} changes", changes.getChanges().size());
    }

    public boolean providerExists(String providerId) {
        return providerRepository.existsById(providerId);
    }

    @Transactional
    public void ingestProvider(CqcProviderDetail detail) {
        Provider provider = providerRepository.findById(detail.getProviderId())
                .orElse(new Provider());

        provider.setProviderId(detail.getProviderId());
        provider.setProviderName(detail.getName());
        provider.setOrganisationType(detail.getType());
        provider.setUpdatedAt(OffsetDateTime.now());

        if (detail.getCompaniesHouseNumber() != null && !detail.getCompaniesHouseNumber().isBlank()) {
            provider.setCompaniesHouseNo(detail.getCompaniesHouseNumber());
        }
        if (provider.getIngestedAt() == null) {
            provider.setIngestedAt(OffsetDateTime.now());
        }

        providerRepository.save(provider);

        if (detail.getLocationIds() != null) {
            detail.getLocationIds().forEach(locationId ->
                    cqcClient.fetchLocationDetail(locationId).ifPresent(this::ingestLocation)
            );
        }
    }

    @Transactional
    public void ingestLocation(CqcLocationDetail detail) {
        Location location = locationRepository.findById(detail.getLocationId())
                .orElse(new Location());

        location.setLocationId(detail.getLocationId());
        location.setLocationName(detail.getName());
        location.setPostcode(detail.getPostalCode());
        location.setRegistrationStatus(detail.getRegistrationStatus());
        location.setUpdatedAt(OffsetDateTime.now());

        location.setAddressLines(buildAddressLines(
                detail.getPostalAddressLine1(),
                detail.getPostalAddressLine2(),
                detail.getPostalAddressTownCity(),
                detail.getPostalAddressCounty()
        ));

        if (detail.getCurrentRatings() != null && detail.getCurrentRatings().getOverall() != null) {
            location.setRating(detail.getCurrentRatings().getOverall().getRating());
        }

        if (detail.getGacServiceTypes() != null) {
            location.setServiceTypes(
                    detail.getGacServiceTypes().stream()
                            .map(CqcServiceType::getName)
                            .toArray(String[]::new)
            );
        }

        if (detail.getSpecialisms() != null) {
            location.setUserBands(
                    detail.getSpecialisms().stream()
                            .map(CqcSpecialism::getName)
                            .toArray(String[]::new)
            );
        }

        providerRepository.findById(detail.getProviderId())
                .ifPresent(location::setProvider);

        if (detail.getOnspdLatitude() != null && detail.getOnspdLongitude() != null) {
            location.setCoordinates(
                    geometryFactory.createPoint(
                            new Coordinate(detail.getOnspdLongitude(), detail.getOnspdLatitude())
                    )
            );
        } else {
            geocodingService.geocode(detail.getPostalCode())
                    .ifPresentOrElse(
                            location::setCoordinates,
                            () -> log.warn("No coordinates for location {} postcode {}",
                                    detail.getLocationId(), detail.getPostalCode())
                    );
        }

        if (location.getIngestedAt() == null) {
            location.setIngestedAt(OffsetDateTime.now());
        }

        locationRepository.save(location);
    }

    private String buildAddressLines(String... parts) {
        // Arrays.stream is used deliberately — List.of() rejects null elements
        // and CQC frequently omits county, causing NPE with List.of()
        return Arrays.stream(parts)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private OffsetDateTime getWatermark() {
        return watermarkRepository.findById(WATERMARK_SOURCE)
                .map(IngestionWatermark::getLastSyncedAt)
                .orElse(OffsetDateTime.parse("2000-01-01T00:00:00Z"));
    }

    private void advanceWatermark(OffsetDateTime to) {
        IngestionWatermark watermark = watermarkRepository.findById(WATERMARK_SOURCE)
                .orElse(new IngestionWatermark());
        watermark.setSource(WATERMARK_SOURCE);
        watermark.setLastSyncedAt(to);
        watermarkRepository.save(watermark);
    }
}
