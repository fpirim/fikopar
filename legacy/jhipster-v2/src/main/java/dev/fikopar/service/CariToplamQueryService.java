package dev.fikopar.service;

import dev.fikopar.domain.Cari;
import dev.fikopar.domain.Cari_;
import dev.fikopar.domain.Sicil_;
import dev.fikopar.repository.CariRepository;
import dev.fikopar.service.criteria.CariCriteria;
import dev.fikopar.service.criteria.CariToplamCriteria;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.CariToplamDTO;
import dev.fikopar.service.mapper.CariMapper;
import dev.fikopar.service.report.CariReportService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.JoinType;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.QueryService;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Service for executing complex queries for {@link Cari} entities in the database.
 * The main input is a {@link CariCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CariToplamDTO} of {@link CariToplamDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CariToplamQueryService extends QueryService<Cari> implements ReportService {

    private final Logger log = LoggerFactory.getLogger(CariToplamQueryService.class);
    private final CariRepository cariRepository;
    private final CariMapper cariMapper;
    private final CariReportService cariReportService;

    public CariToplamQueryService(CariRepository cariRepository, CariMapper cariMapper, CariReportService cariReportService) {
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
    public List<CariToplamDTO> findByCriteria(CariToplamCriteria criteria) {
        log.debug("find by criteria : {}", criteria);

        if (criteria.getTarih() == null || criteria.getTarih().getGreaterThanOrEqual() == null) {
            return new ArrayList<>();
        }

        return cariMapper.toCariToplamDto(
            cariRepository.findCariToplams(
                Optional.ofNullable(criteria.getTarih()).map(LocalDateFilter::getLessThanOrEqual).orElse(null),
                Optional.ofNullable(criteria.getDepotipi()).map(CariToplamCriteria.DepoTipiFilter::getEquals).orElse(null),
                Optional.ofNullable(criteria.getFilter()).map(StringFilter::getContains).orElse(null)
            )
        );
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CariToplamCriteria criteria) {
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
    protected Specification<Cari> createSpecification(CariToplamCriteria criteria) {
        Specification<Cari> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null

            if (criteria.getDepotipi() != null) {
                specification = specification.and(buildSpecification(criteria.getDepotipi(), Cari_.depotipi));
            }

            specification = specification.and(buildRangeSpecification(criteria.getTarih(), Cari_.tarih));

            if (criteria.getFilter() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getFilter(), root -> root.join(Cari_.sicil, JoinType.LEFT).get(Sicil_.hesapno))
                    );

                specification =
                    specification.or(
                        buildSpecification(criteria.getFilter(), root -> root.join(Cari_.sicil, JoinType.LEFT).get(Sicil_.hesapadi))
                    );

                specification =
                    specification.or(
                        buildSpecification(criteria.getFilter(), root -> root.join(Cari_.sicil, JoinType.LEFT).get(Sicil_.ilce))
                    );

                specification =
                    specification.or(
                        buildSpecification(criteria.getFilter(), root -> root.join(Cari_.sicil, JoinType.LEFT).get(Sicil_.sehir))
                    );
            }
        }
        return specification;
    }

    @Override
    public void generateReport(Criteria criteria, HttpServletResponse response) throws IOException {
        List<CariToplamDTO> caris = findByCriteria((CariToplamCriteria) criteria);
        //        XSSFWorkbook workbook = cariReportService.write(caris, (CariCriteria) criteria);
        //
        //        ServletOutputStream outputStream = response.getOutputStream();
        //        workbook.write(outputStream);
        //        workbook.close();
        //        outputStream.close();
    }
}
