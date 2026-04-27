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
}
