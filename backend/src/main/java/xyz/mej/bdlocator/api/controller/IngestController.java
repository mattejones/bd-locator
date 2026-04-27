package xyz.mej.bdlocator.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.mej.bdlocator.ingestion.cqc.CqcIngestionService;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final CqcIngestionService cqcIngestionService;

    /**
     * Triggers a delta sync from the CQC Changes endpoint.
     * Safe to call repeatedly — uses watermark to avoid reprocessing.
     */
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        log.info("Manual delta sync triggered via API");
        cqcIngestionService.runDeltaSync();
        return Map.of(
                "status", "ok",
                "mode", "delta",
                "triggeredAt", OffsetDateTime.now().toString()
        );
    }

    /**
     * Triggers a full bootstrap ingestion from CQC.
     * Long-running — intended for initial seeding only.
     */
    @PostMapping("/bootstrap")
    public Map<String, Object> bootstrap() {
        log.info("Bootstrap ingestion triggered via API");
        cqcIngestionService.runBootstrap();
        return Map.of(
                "status", "ok",
                "mode", "bootstrap",
                "triggeredAt", OffsetDateTime.now().toString()
        );
    }
}
