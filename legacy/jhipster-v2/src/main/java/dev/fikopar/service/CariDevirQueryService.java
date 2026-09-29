package dev.fikopar.service;

import dev.fikopar.domain.*; // for static metamodels
import dev.fikopar.domain.CariDevir;
import dev.fikopar.repository.CariDevirRepository;
import dev.fikopar.service.criteria.CariDevirCriteria;
import dev.fikopar.service.dto.CariDevirDTO;
import dev.fikopar.service.mapper.CariDevirMapper;
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
 * Service for executing complex queries for {@link CariDevir} entities in the database.
 * The main input is a {@link CariDevirCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CariDevirDTO} or a {@link Page} of {@link CariDevirDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CariDevirQueryService extends QueryService<CariDevir> {

    private final Logger log = LoggerFactory.getLogger(CariDevirQueryService.class);

    private final CariDevirRepository cariDevirRepository;

    private final CariDevirMapper cariDevirMapper;

    public CariDevirQueryService(CariDevirRepository cariDevirRepository, CariDevirMapper cariDevirMapper) {
        this.cariDevirRepository = cariDevirRepository;
        this.cariDevirMapper = cariDevirMapper;
    }

    /**
     * Return a {@link List} of {@link CariDevirDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CariDevirDTO> findByCriteria(CariDevirCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CariDevir> specification = createSpecification(criteria);
        return cariDevirMapper.toDto(cariDevirRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CariDevirDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CariDevirDTO> findByCriteria(CariDevirCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CariDevir> specification = createSpecification(criteria);
        return cariDevirRepository.findAll(specification, page).map(cariDevirMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CariDevirCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CariDevir> specification = createSpecification(criteria);
        return cariDevirRepository.count(specification);
    }

    /**
     * Function to convert {@link CariDevirCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CariDevir> createSpecification(CariDevirCriteria criteria) {
        Specification<CariDevir> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), CariDevir_.id));
            }
            if (criteria.getDepotipi() != null) {
                specification = specification.or(buildSpecification(criteria.getDepotipi(), CariDevir_.depotipi));
            }
            if (criteria.getYil() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getYil(), CariDevir_.yil));
            }
            if (criteria.getBorc() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getBorc(), CariDevir_.borc));
            }
            if (criteria.getAlacak() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getAlacak(), CariDevir_.alacak));
            }
            if (criteria.getSicilId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getSicilId(), root -> root.join(CariDevir_.sicil, JoinType.LEFT).get(Sicil_.id))
                    );
            }
            if (criteria.getCariId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getCariId(), root -> root.join(CariDevir_.cari, JoinType.LEFT).get(Cari_.id))
                    );
            }
        }
        return specification;
    }
}
