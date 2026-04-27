package com.bdlocator.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @Column(name = "location_id")
    private String locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "address_lines")
    private String addressLines;

    @Column(name = "postcode", nullable = false)
    private String postcode;

    // Stored as PostGIS geometry — accessed via native queries for spatial ops
    @Column(name = "coordinates", columnDefinition = "GEOMETRY(Point, 4326)")
    private org.locationtech.jts.geom.Point coordinates;

    @Column(name = "service_types", columnDefinition = "TEXT[]")
    private String[] serviceTypes;

    @Column(name = "user_bands", columnDefinition = "TEXT[]")
    private String[] userBands;

    @Column(name = "rating")
    private String rating;

    @Column(name = "registration_status")
    private String registrationStatus;

    @Column(name = "ingested_at")
    private OffsetDateTime ingestedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
