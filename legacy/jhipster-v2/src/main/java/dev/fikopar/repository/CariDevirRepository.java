package dev.fikopar.repository;

import dev.fikopar.domain.CariDevir;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CariDevir entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CariDevirRepository extends JpaRepository<CariDevir, Long>, JpaSpecificationExecutor<CariDevir> {}
