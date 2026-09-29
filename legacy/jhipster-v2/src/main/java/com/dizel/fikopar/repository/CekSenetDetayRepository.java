package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.CekSenetDetay;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CekSenetDetay entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CekSenetDetayRepository extends JpaRepository<CekSenetDetay, Long>, JpaSpecificationExecutor<CekSenetDetay> {
    List<CekSenetDetay> findAllByCekSenetId(Long id);
}
