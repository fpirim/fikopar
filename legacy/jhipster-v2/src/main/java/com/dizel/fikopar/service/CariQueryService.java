package com.dizel.fikopar.service;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.Cari_;
import com.dizel.fikopar.domain.Sicil_;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.CariRepository;
import com.dizel.fikopar.repository.custom.DevirToplamlari;
import com.dizel.fikopar.service.criteria.CariCriteria;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.mapper.CariMapper;
import com.dizel.fikopar.service.report.CariReportService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.JoinType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Cari} entities in the database.
 * The main input is a {@link CariCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CariDTO} or a {@link Page} of {@link CariDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CariQueryService extends QueryService<Cari> implements ReportService {

    private final Logger log = LoggerFactory.getLogger(CariQueryService.class);

    private final CariRepository cariRepository;

    private final CariMapper cariMapper;
    private final CariReportService cariReportService;

    public CariQueryService(CariRepository cariRepository, CariMapper cariMapper, CariReportService cariReportService) {
        this.cariRepository = cariRepository;
        this.cariMapper = cariMapper;
        this.cariReportService = cariReportService;
    }

    /**
     * Return a {@link List} of {@link CariDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CariDTO> findByCriteria(CariCriteria criteria) {
        log.debug("find by criteria : {}", criteria);

        if (criteria.getTarih() == null || criteria.getTarih().getGreaterThanOrEqual() == null || criteria.getSicilId() == null) {
            return new ArrayList<>();
        }

        Long sicilId = criteria.getSicilId().getEquals();
        LocalDate tarih = criteria.getTarih().getGreaterThanOrEqual();
        DepoTipi depoTipi = criteria.getDepotipi() == null ? null : criteria.getDepotipi().getEquals();
        List<DevirToplamlari> devirToplamlariList = cariRepository.sumByDepoTipiBeforeTarih(depoTipi, tarih, sicilId);

        final Specification<Cari> specification = createSpecification(criteria);
        List<CariDTO> caris = cariMapper.toDto(cariRepository.findAll(specification, Sort.by("tarih").ascending()));

        devirToplamlariList.forEach(devir -> {
            if (devir.getAlacak().compareTo(BigDecimal.ZERO) == 0 && devir.getBorc().compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            CariDTO cariDTO = new CariDTO();
            cariDTO.setEvrakno("");
            cariDTO.setTarih(tarih);
            cariDTO.setVade(tarih);
            cariDTO.setTipi(EvrakTipi.DEVIR);
            cariDTO.setId(devir.getDepoTipi() == DepoTipi.DEPO1 ? -2L : -1L);
            cariDTO.setDepotipi(devir.getDepoTipi());
            cariDTO.setAciklama("Önceki Dönemden Devir");
            cariDTO.setBorc(BigDecimal.ZERO);
            cariDTO.setAlacak(BigDecimal.ZERO);
            if (devir.getAlacak().compareTo(devir.getBorc()) > 0) {
                cariDTO.setAlacak(devir.getAlacak().subtract(devir.getBorc()));
            } else {
                cariDTO.setBorc(devir.getBorc().subtract(devir.getAlacak()));
            }
            caris.add(0, cariDTO);
        });

        return caris;
    }

    /**
     * Return a {@link Page} of {@link CariDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CariDTO> findByCriteria(CariCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Cari> specification = createSpecification(criteria);
        return cariRepository.findAll(specification, page).map(cariMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CariCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Cari> specification = createSpecification(criteria);
        return cariRepository.count(specification);
    }

    /**
     * Function to convert {@link CariCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Cari> createSpecification(CariCriteria criteria) {
        Specification<Cari> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            //            if (criteria.getId() != null) {
            //                specification = specification.or(buildRangeSpecification(criteria.getId(), Cari_.id));
            //            }
            //            if (criteria.getTipi() != null) {
            //                specification = specification.or(buildSpecification(criteria.getTipi(), Cari_.tipi));
            //            }
            //            if (criteria.getEvrakno() != null) {
            //                specification = specification.or(buildStringSpecification(criteria.getEvrakno(), Cari_.evrakno));
            //            }
            if (criteria.getDepotipi() != null) {
                specification = specification.and(buildSpecification(criteria.getDepotipi(), Cari_.depotipi));
            }

            specification = specification.and(buildRangeSpecification(criteria.getTarih(), Cari_.tarih));

            //            if (criteria.getVade() != null) {
            //                specification = specification.or(buildRangeSpecification(criteria.getVade(), Cari_.vade));
            //            }
            //            if (criteria.getGun() != null) {
            //                specification = specification.or(buildRangeSpecification(criteria.getGun(), Cari_.gun));
            //            }
            //            if (criteria.getAciklama() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getAciklama(), Cari_.aciklama));
            //            }
            //            if (criteria.getBorc() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getBorc(), Cari_.borc));
            //            }
            //            if (criteria.getAlacak() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getAlacak(), Cari_.alacak));
            //            }

            specification =
                specification.and(buildSpecification(criteria.getSicilId(), root -> root.join(Cari_.sicil, JoinType.LEFT).get(Sicil_.id)));
        }
        return specification;
    }

    @Override
    public void generateReport(Criteria criteria, HttpServletResponse response) throws IOException {
        List<CariDTO> caris = findByCriteria((CariCriteria) criteria);

        XSSFWorkbook workbook = cariReportService.write(caris, (CariCriteria) criteria);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
