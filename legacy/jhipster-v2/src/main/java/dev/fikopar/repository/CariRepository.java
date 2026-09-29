package dev.fikopar.repository;

import dev.fikopar.domain.Cari;
import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.repository.custom.CariToplam;
import dev.fikopar.repository.custom.DevirToplamlari;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Cari entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CariRepository extends JpaRepository<Cari, Long>, JpaSpecificationExecutor<Cari> {
    @Query(
        "select new dev.fikopar.repository.custom.DevirToplamlari(c.depotipi, sum(c.borc), sum(c.alacak)) from Cari c " +
        "left join c.sicil s where c.tarih < :tarih and s.id = :sicilId and (:depoTipi is null or c.depotipi = :depoTipi) group by c.depotipi order by c.depotipi"
    )
    List<DevirToplamlari> sumByDepoTipiBeforeTarih(
        @Param("depoTipi") DepoTipi depoTipi,
        @Param("tarih") LocalDate tarih,
        @Param("sicilId") Long sicilId
    );

    @Query(
        "select new dev.fikopar.repository.custom.CariToplam(s.id, s.hesapno, s.hesapadi, s.ilce, s.sehir, sum(c.borc), sum(c.alacak)) from Cari c join c.sicil s " +
        "where (cast(:tarih as date) is null or c.tarih < :tarih) " +
        "and (:depotipi is null or c.depotipi = :depotipi) " +
        "and (:filter is null or (LOWER(s.hesapno) like LOWER(concat('%', concat(:filter, '%'))) " +
        "or LOWER(s.hesapadi) like LOWER(concat('%', concat(:filter, '%'))) " +
        "or LOWER(s.ilce) like LOWER(concat('%', concat(:filter, '%'))) " +
        "or LOWER(s.sehir) like LOWER(concat('%', concat(:filter, '%'))))) " +
        "group by s.id, s.hesapno, s.hesapadi, s.ilce, s.sehir order by s.hesapno"
    )
    List<CariToplam> findCariToplams(@Param("tarih") LocalDate tarih, @Param("depotipi") DepoTipi depotipi, @Param("filter") String filter);
}
