package xyz.mej.bdlocator.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionWatermarkRepository extends JpaRepository<IngestionWatermark, String> {
}
