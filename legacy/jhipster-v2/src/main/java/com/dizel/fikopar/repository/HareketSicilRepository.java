package com.dizel.fikopar.repository;

import com.dizel.fikopar.domain.HareketSicil;
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
 * Spring Data JPA repository for the HareketSicil entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HareketSicilRepository extends JpaRepository<HareketSicil, Long>, JpaSpecificationExecutor<HareketSicil> {
    @Override
    @EntityGraph(attributePaths = { "hareketDetays" })
    @Query(value = "SELECT h FROM HareketSicil h left join h.hareketDetays WHERE h.id = :id")
    Optional<HareketSicil> findById(@Param("id") Long id);

    @Query(
        "select max(h.evrakno) from HareketSicil h where h.evraktipi = :evraktipi and h.depotipi = :depotipi and h.tarihi >= :baslangic and h.tarihi <= :bitis"
    )
    Optional<String> findMaxEvrakNo(
        @Param("evraktipi") EvrakTipi evraktipi,
        @Param("depotipi") DepoTipi depotipi,
        @Param("baslangic") LocalDate baslangic,
        @Param("bitis") LocalDate bitis
    );
}
