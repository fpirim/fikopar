package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.CekSenet;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CekSenet entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CekSenetRepository extends JpaRepository<CekSenet, Long>, JpaSpecificationExecutor<CekSenet> {
    @Override
    @EntityGraph(attributePaths = { "cekSenetDetays", "cekSenetDetays.makbuz" })
    @Query(value = "SELECT c FROM CekSenet c left join c.cekSenetDetays d left join d.makbuz WHERE c.id = :id")
    Optional<CekSenet> findById(@Param("id") Long id);

    @Query(
        "select max(c.ceksenetno) from CekSenet c where c.evraktipi = :evraktipi and c.depotipi = :depotipi and c.tarihi >= :baslangic and c.tarihi <= :bitis"
    )
    Optional<String> findMaxCekSenetNo(
        @Param("evraktipi") EvrakTipi evraktipi,
        @Param("depotipi") DepoTipi depotipi,
        @Param("baslangic") LocalDate baslangic,
        @Param("bitis") LocalDate bitis
    );
}
