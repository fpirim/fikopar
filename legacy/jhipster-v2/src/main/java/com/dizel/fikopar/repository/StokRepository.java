package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.Stok;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Stok entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StokRepository extends JpaRepository<Stok, Long>, JpaSpecificationExecutor<Stok> {}
