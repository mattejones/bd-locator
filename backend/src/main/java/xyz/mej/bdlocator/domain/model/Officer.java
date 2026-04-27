package xyz.mej.bdlocator.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "officers")
@Getter
@Setter
@NoArgsConstructor
public class Officer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "role")
    private String role;

    @Column(name = "appointed_on")
    private LocalDate appointedOn;

    @Column(name = "resigned_on")
    private LocalDate resignedOn;

    @Column(name = "ingested_at")
    private OffsetDateTime ingestedAt;
}
