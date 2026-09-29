package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Makbuz entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MakbuzRepository extends JpaRepository<Makbuz, Long>, JpaSpecificationExecutor<Makbuz> {
    @Query(
        "select max(m.makbuzno) from Makbuz m where m.evraktipi = :evraktipi and m.depotipi = :depotipi and m.tarihi >= :baslangic and m.tarihi <= :bitis"
    )
    Optional<String> findMaxMakbuzNo(
        @Param("evraktipi") EvrakTipi evraktipi,
        @Param("depotipi") DepoTipi depotipi,
        @Param("baslangic") LocalDate baslangic,
        @Param("bitis") LocalDate bitis
    );
}
