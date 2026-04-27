package xyz.mej.bdlocator.domain.repository;

import xyz.mej.bdlocator.domain.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    /**
     * Returns locations within a given radius (km) of an origin point,
     * ordered by distance ascending. Uses PostGIS ST_DWithin for index-friendly
     * distance filtering, ST_Distance for ordering.
     */
    @Query(value = """
            SELECT l.* FROM locations l
            WHERE l.coordinates IS NOT NULL
              AND ST_DWithin(
                    l.coordinates::geography,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radiusMetres
                  )
            ORDER BY ST_Distance(
                l.coordinates::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            )
            """, nativeQuery = true)
    List<Location> findWithinRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMetres") double radiusMetres
    );

    /**
     * Returns the distance in metres between a location and an origin point.
     * Used by the scoring engine to compute proximity decay per candidate.
     */
    @Query(value = """
            SELECT ST_Distance(
                l.coordinates::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
            )
            FROM locations l
            WHERE l.location_id = :locationId
            """, nativeQuery = true)
    Double distanceMetresTo(
            @Param("locationId") String locationId,
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    /**
     * Counts how many locations belong to a provider — used as the scale signal.
     */
    long countByProviderProviderId(String providerId);
}
