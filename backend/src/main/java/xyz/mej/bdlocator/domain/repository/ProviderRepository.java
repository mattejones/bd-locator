package xyz.mej.bdlocator.domain.repository;

import xyz.mej.bdlocator.domain.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, String> {

    Optional<Provider> findByCompaniesHouseNo(String companiesHouseNo);
}
