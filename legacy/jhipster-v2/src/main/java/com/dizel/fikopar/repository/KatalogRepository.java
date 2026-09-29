package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.Katalog;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Katalog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface KatalogRepository extends JpaRepository<Katalog, Long>, JpaSpecificationExecutor<Katalog> {}
