package dev.fikopar.service;

import dev.fikopar.domain.*; // for static metamodels
import dev.fikopar.domain.Katalog;
import dev.fikopar.repository.KatalogRepository;
import dev.fikopar.service.criteria.KatalogCriteria;
import dev.fikopar.service.dto.KatalogDTO;
import dev.fikopar.service.mapper.KatalogMapper;
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
 * Service for executing complex queries for {@link Katalog} entities in the database.
 * The main input is a {@link KatalogCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link KatalogDTO} or a {@link Page} of {@link KatalogDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class KatalogQueryService extends QueryService<Katalog> {

    private final Logger log = LoggerFactory.getLogger(KatalogQueryService.class);

    private final KatalogRepository katalogRepository;

    private final KatalogMapper katalogMapper;

    public KatalogQueryService(KatalogRepository katalogRepository, KatalogMapper katalogMapper) {
        this.katalogRepository = katalogRepository;
        this.katalogMapper = katalogMapper;
    }

    /**
     * Return a {@link List} of {@link KatalogDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<KatalogDTO> findByCriteria(KatalogCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Katalog> specification = createSpecification(criteria);
        return katalogMapper.toDto(katalogRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link KatalogDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<KatalogDTO> findByCriteria(KatalogCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Katalog> specification = createSpecification(criteria);
        return katalogRepository.findAll(specification, page).map(katalogMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(KatalogCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Katalog> specification = createSpecification(criteria);
        return katalogRepository.count(specification);
    }

    /**
     * Function to convert {@link KatalogCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Katalog> createSpecification(KatalogCriteria criteria) {
        Specification<Katalog> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), Katalog_.id));
            }
            if (criteria.getKatalogadi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getKatalogadi(), Katalog_.katalogadi));
            }
            if (criteria.getStoksId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getStoksId(), root -> root.join(Katalog_.stoks, JoinType.LEFT).get(Stok_.id))
                    );
            }
        }
        return specification;
    }
}
