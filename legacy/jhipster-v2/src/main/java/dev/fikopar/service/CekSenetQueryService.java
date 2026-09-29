package dev.fikopar.service;

import dev.fikopar.domain.CekSenet;
import dev.fikopar.domain.CekSenet_;
import dev.fikopar.domain.Sicil_;
import dev.fikopar.repository.CekSenetRepository;
import dev.fikopar.service.criteria.CekSenetCriteria;
import dev.fikopar.service.dto.CekSenetDTO;
import dev.fikopar.service.mapper.CekSenetMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CekSenet} entities in the database.
 * The main input is a {@link CekSenetCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CekSenetDTO} or a {@link Page} of {@link CekSenetDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CekSenetQueryService extends QueryService<CekSenet> {

    private final Logger log = LoggerFactory.getLogger(CekSenetQueryService.class);

    private final CekSenetRepository cekSenetRepository;

    private final CekSenetMapper cekSenetMapper;

    public CekSenetQueryService(CekSenetRepository cekSenetRepository, CekSenetMapper cekSenetMapper) {
        this.cekSenetRepository = cekSenetRepository;
        this.cekSenetMapper = cekSenetMapper;
    }

    /**
     * Return a {@link List} of {@link CekSenetDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CekSenetDTO> findByCriteria(CekSenetCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CekSenet> specification = createSpecification(criteria);
        return cekSenetMapper.toDto(cekSenetRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CekSenetDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CekSenetDTO> findByCriteria(CekSenetCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CekSenet> specification = createSpecification(criteria);
        return cekSenetRepository.findAll(specification, page).map(cekSenetMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CekSenetCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CekSenet> specification = createSpecification(criteria);
        return cekSenetRepository.count(specification);
    }

    /**
     * Function to convert {@link CekSenetCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CekSenet> createSpecification(CekSenetCriteria criteria) {
        Specification<CekSenet> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            //            if (criteria.getId() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getId(), CekSenet_.id));
            //            }
            //            if (criteria.getCeksenetno() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getCeksenetno(), CekSenet_.ceksenetno));
            //            }
            //            if (criteria.getVade() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getVade(), CekSenet_.vade));
            //            }
            //            if (criteria.getAciklama() != null) {
            //                specification = specification.and(buildStringSpecification(criteria.getAciklama(), CekSenet_.aciklama));
            //            }
            //            if (criteria.getTutar() != null) {
            //                specification = specification.and(buildRangeSpecification(criteria.getTutar(), CekSenet_.tutar));
            //            }

            //            if (criteria.getCekSenetDetaysId() != null) {
            //                specification =
            //                    specification.and(
            //                        buildSpecification(
            //                            criteria.getCekSenetDetaysId(),
            //                            root -> root.join(CekSenet_.cekSenetDetays, JoinType.LEFT).get(CekSenetDetay_.id)
            //                        )
            //                    );
            //            }
            specification = specification.and(buildRangeSpecification(criteria.getVade(), CekSenet_.vade));
            specification = specification.and(buildRangeSpecification(criteria.getTarihi(), CekSenet_.tarihi));
            if (criteria.getDepotipi() != null) {
                specification = specification.and(buildSpecification(criteria.getDepotipi(), CekSenet_.depotipi));
            }
            if (criteria.getEvraktipi() != null) {
                specification = specification.and(buildSpecification(criteria.getEvraktipi(), CekSenet_.evraktipi));
            }
            if (criteria.getSicilId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getSicilId(), root -> root.join(CekSenet_.sicil, JoinType.LEFT).get(Sicil_.id))
                    );
            }
        }
        return specification;
    }
}
