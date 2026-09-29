package dev.fikopar.repository;

import dev.fikopar.domain.Sicil;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Sicil entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SicilRepository extends JpaRepository<Sicil, Long>, JpaSpecificationExecutor<Sicil> {}
