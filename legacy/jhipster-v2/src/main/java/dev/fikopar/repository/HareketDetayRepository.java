package dev.fikopar.repository;

import dev.fikopar.domain.HareketDetay;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the HareketDetay entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HareketDetayRepository extends JpaRepository<HareketDetay, Long>, JpaSpecificationExecutor<HareketDetay> {}
