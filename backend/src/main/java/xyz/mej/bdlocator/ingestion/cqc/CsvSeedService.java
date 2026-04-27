package xyz.mej.bdlocator.ingestion.cqc;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvSeedService {

    private final CqcIngestionService cqcIngestionService;
    private final CqcClient cqcClient;

    private static final int HEADER_ROW_INDEX = 4;
    private static final String LOCATION_ID_COLUMN = "CQC Location ID (for office use only)";
    private static final String POSTCODE_COLUMN = "Postcode";

    public int seedFromCsv(String csvPath, Set<String> postcodePrefixes) throws IOException {
        log.info("Starting CSV seed from: {} filtering by prefixes: {}", csvPath, postcodePrefixes);

        List<String> locationIds = extractLocationIds(csvPath, postcodePrefixes);
        log.info("Found {} locations matching postcode prefixes {}", locationIds.size(), postcodePrefixes);

        int ingested = 0;
        int failed = 0;

        for (String locationId : locationIds) {
            try {
                var detail = cqcClient.fetchLocationDetail(locationId);

                if (detail.isEmpty()) {
                    log.warn("No detail returned for location {}", locationId);
                    failed++;
                    continue;
                }

                String providerId = detail.get().getProviderId();
                if (providerId != null && !cqcIngestionService.providerExists(providerId)) {
                    cqcClient.fetchProviderDetail(providerId)
                            .ifPresent(cqcIngestionService::ingestProvider);
                }

                cqcIngestionService.ingestLocation(detail.get());
                ingested++;

                if (ingested % 50 == 0) {
                    log.info("Progress: {}/{} locations ingested", ingested, locationIds.size());
                }

            } catch (Exception e) {
                // Pass e as final arg so SLF4J prints the full stack trace
                log.warn("Failed to ingest location {} [{}]: {}",
                        locationId, e.getClass().getSimpleName(), e.getMessage(), e);
                failed++;
            }
        }

        log.info("CSV seed complete — {} ingested, {} failed", ingested, failed);
        return ingested;
    }

    private List<String> extractLocationIds(String csvPath, Set<String> postcodePrefixes) throws IOException {
        List<String> ids = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] headers = null;
            for (int i = 0; i <= HEADER_ROW_INDEX; i++) {
                try {
                    headers = reader.readNext();
                } catch (CsvValidationException e) {
                    throw new IOException("Failed to read CSV header row " + i, e);
                }
                if (headers == null) throw new IOException("CSV ended before header row");
            }

            int locationIdCol = findColumn(headers, LOCATION_ID_COLUMN);
            int postcodeCol = findColumn(headers, POSTCODE_COLUMN);

            log.info("Location ID column: {}, Postcode column: {}", locationIdCol, postcodeCol);

            String[] row;
            while (true) {
                try {
                    row = reader.readNext();
                } catch (CsvValidationException e) {
                    log.warn("Skipping malformed row: {}", e.getMessage());
                    continue;
                }
                if (row == null) break;
                if (row.length <= Math.max(locationIdCol, postcodeCol)) continue;

                String postcode = row[postcodeCol].trim().toUpperCase();
                String locationId = row[locationIdCol].trim();

                if (!locationId.isBlank() && matchesPrefix(postcode, postcodePrefixes)) {
                    ids.add(locationId);
                }
            }
        }

        return ids;
    }

    private int findColumn(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) return i;
        }
        throw new IllegalArgumentException("Column not found: '" + columnName + "'");
    }

    private boolean matchesPrefix(String postcode, Set<String> prefixes) {
        return prefixes.stream().anyMatch(p -> postcode.startsWith(p.toUpperCase()));
    }
}
