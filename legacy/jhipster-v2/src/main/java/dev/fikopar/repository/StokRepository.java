package dev.fikopar.repository;

import dev.fikopar.domain.Stok;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Stok entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StokRepository extends JpaRepository<Stok, Long>, JpaSpecificationExecutor<Stok> {}
