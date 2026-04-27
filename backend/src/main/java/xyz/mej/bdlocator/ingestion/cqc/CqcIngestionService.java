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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Value("${app.cqc.page-size:1000}")
    private int pageSize;

    private static final String WATERMARK_SOURCE = "cqc";

    /**
     * Bootstrap mode — pages through the full CQC provider list.
     * Intended for initial seeding. Safe to re-run; uses upsert semantics.
     */
    public void runBootstrap() {
        log.info("Starting CQC bootstrap ingestion");
        int page = 1;
        int totalPages;

        do {
            log.info("Fetching CQC provider page {}", page);
            CqcProviderPageResponse response = cqcClient.fetchProviderPage(page, pageSize);
            totalPages = response.getTotalPages();

            response.getProviders().forEach(summary ->
                    cqcClient.fetchProviderDetail(summary.getProviderId())
                            .ifPresent(this::ingestProvider)
            );

            page++;
        } while (page <= totalPages);

        advanceWatermark(OffsetDateTime.now());
        log.info("CQC bootstrap ingestion complete");
    }

    /**
     * On-demand mode — fetches only records changed since the last watermark.
     * Safe to invoke on every user-triggered refresh.
     */
    public void runDeltaSync() {
        OffsetDateTime since = getWatermark();
        OffsetDateTime until = OffsetDateTime.now();
        log.info("Starting CQC delta sync from {} to {}", since, until);

        CqcChangesResponse changes = cqcClient.fetchChanges(since, until);

        if (changes == null || changes.getChanges() == null) {
            log.info("No CQC changes found in window");
            advanceWatermark(until);
            return;
        }

        changes.getChanges().forEach(change -> {
            if ("location".equals(change.getType()) && change.getLocationId() != null) {
                cqcClient.fetchLocationDetail(change.getLocationId())
                        .ifPresent(this::ingestLocation);
            } else if ("provider".equals(change.getType()) && change.getProviderId() != null) {
                cqcClient.fetchProviderDetail(change.getProviderId())
                        .ifPresent(this::ingestProvider);
            }
        });

        advanceWatermark(until);
        log.info("CQC delta sync complete — {} changes processed", changes.getChanges().size());
    }

    @Transactional
    protected void ingestProvider(CqcProviderDetail detail) {
        Provider provider = providerRepository.findById(detail.getProviderId())
                .orElse(new Provider());

        provider.setProviderId(detail.getProviderId());
        provider.setProviderName(detail.getName());
        provider.setOrganisationType(detail.getType());
        provider.setUpdatedAt(OffsetDateTime.now());

        // Only set CH number if present — never overwrite an existing value with null
        if (detail.getCompaniesHouseNumber() != null && !detail.getCompaniesHouseNumber().isBlank()) {
            provider.setCompaniesHouseNo(detail.getCompaniesHouseNumber());
        }

        if (provider.getIngestedAt() == null) {
            provider.setIngestedAt(OffsetDateTime.now());
        }

        providerRepository.save(provider);

        if (detail.getLocations() != null) {
            detail.getLocations().forEach(loc ->
                    cqcClient.fetchLocationDetail(loc.getLocationId())
                            .ifPresent(this::ingestLocation)
            );
        }
    }

    @Transactional
    protected void ingestLocation(CqcLocationDetail detail) {
        Location location = locationRepository.findById(detail.getLocationId())
                .orElse(new Location());

        location.setLocationId(detail.getLocationId());
        location.setLocationName(detail.getName());
        location.setPostcode(detail.getPostalCode());
        location.setRegistrationStatus(detail.getRegistrationStatus());
        location.setUpdatedAt(OffsetDateTime.now());

        if (detail.getAddress() != null) {
            location.setAddressLines(buildAddressLines(detail.getAddress()));
        }

        if (detail.getCurrentRatings() != null
                && detail.getCurrentRatings().getOverall() != null) {
            location.setRating(detail.getCurrentRatings().getOverall().getRating());
        }

        if (detail.getGacServiceTypes() != null) {
            location.setServiceTypes(
                    detail.getGacServiceTypes().stream()
                            .map(CqcServiceType::getName)
                            .toArray(String[]::new)
            );
        }

        if (detail.getServiceUserBands() != null) {
            location.setUserBands(
                    detail.getServiceUserBands().stream()
                            .map(CqcUserBand::getName)
                            .toArray(String[]::new)
            );
        }

        // Attach provider if it exists — don't block ingestion if it doesn't
        providerRepository.findById(detail.getProviderId())
                .ifPresent(location::setProvider);

        // Geocode — failure is logged but never blocks the record being saved
        geocodingService.geocode(detail.getPostalCode())
                .ifPresentOrElse(
                        location::setCoordinates,
                        () -> log.warn("No coordinates resolved for location {} postcode {}",
                                detail.getLocationId(), detail.getPostalCode())
                );

        if (location.getIngestedAt() == null) {
            location.setIngestedAt(OffsetDateTime.now());
        }

        locationRepository.save(location);
    }

    private String buildAddressLines(CqcAddress address) {
        return List.of(
                nullToEmpty(address.getAddressLine1()),
                nullToEmpty(address.getAddressLine2()),
                nullToEmpty(address.getCity()),
                nullToEmpty(address.getCounty())
        ).stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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
