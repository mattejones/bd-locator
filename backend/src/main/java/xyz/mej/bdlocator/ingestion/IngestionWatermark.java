package xyz.mej.bdlocator.ingestion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ingestion_watermarks")
@Getter
@Setter
public class IngestionWatermark {

    @Id
    @Column(name = "source")
    private String source;

    @Column(name = "last_synced_at", nullable = false)
    private OffsetDateTime lastSyncedAt;
}
