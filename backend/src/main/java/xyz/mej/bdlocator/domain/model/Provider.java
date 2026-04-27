package xyz.mej.bdlocator.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
public class Provider {

    @Id
    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(name = "companies_house_no")
    private String companiesHouseNo;

    @Column(name = "organisation_type")
    private String organisationType;

    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    private List<Location> locations;

    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    private List<Officer> officers;

    @Column(name = "ingested_at")
    private OffsetDateTime ingestedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
