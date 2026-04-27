package xyz.mej.bdlocator.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.mej.bdlocator.ingestion.cqc.CqcIngestionService;
import xyz.mej.bdlocator.ingestion.cqc.CsvSeedService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final CqcIngestionService cqcIngestionService;
    private final CsvSeedService csvSeedService;

    /**
     * Seeds from a locally downloaded CQC care directory CSV.
     *
     * @param csvPath         absolute path to the CSV file on the server filesystem
     * @param postcodePrefixes postcode prefixes to include e.g. SG, AL (defaults to Letchworth area)
     *
     * Example:
     * curl -X POST "http://localhost:8080/api/ingest/csv?csvPath=/tmp/cqc.csv&postcodePrefix=SG&postcodePrefix=AL"
     */
    @PostMapping("/csv")
    public Map<String, Object> csv(
            @RequestParam String csvPath,
            @RequestParam(name = "postcodePrefix", required = false) List<String> postcodePrefixes
    ) throws IOException {
        Set<String> prefixes = (postcodePrefixes != null && !postcodePrefixes.isEmpty())
                ? Set.copyOf(postcodePrefixes)
                : Set.of("SG", "AL", "EN", "LU");

        log.info("CSV seed triggered — file: {}, prefixes: {}", csvPath, prefixes);
        int count = csvSeedService.seedFromCsv(csvPath, prefixes);

        return Map.of(
                "status", "ok",
                "mode", "csv",
                "csvPath", csvPath,
                "postcodePrefixes", prefixes,
                "locationsIngested", count,
                "triggeredAt", OffsetDateTime.now().toString()
        );
    }

    /**
     * Regional bootstrap via providers API.
     */
    @PostMapping("/regional")
    public Map<String, Object> regional(
            @RequestParam(defaultValue = "East of England") String region
    ) {
        log.info("Regional bootstrap triggered for: {}", region);
        cqcIngestionService.runRegionalBootstrap(region);
        return Map.of("status", "ok", "mode", "regional", "region", region,
                "triggeredAt", OffsetDateTime.now().toString());
    }

    /**
     * Delta sync from watermark.
     */
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        log.info("Delta sync triggered");
        cqcIngestionService.runDeltaSync();
        return Map.of("status", "ok", "mode", "delta",
                "triggeredAt", OffsetDateTime.now().toString());
    }

    /**
     * Full bootstrap — all ~63k providers. Long-running. Use /csv instead.
     */
    @PostMapping("/bootstrap")
    public Map<String, Object> bootstrap() {
        log.info("Full bootstrap triggered");
        cqcIngestionService.runBootstrap();
        return Map.of("status", "ok", "mode", "bootstrap",
                "triggeredAt", OffsetDateTime.now().toString());
    }
}
