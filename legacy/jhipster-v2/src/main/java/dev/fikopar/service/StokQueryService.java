package dev.fikopar.service;

import dev.fikopar.domain.*; // for static metamodels
import dev.fikopar.domain.Stok;
import dev.fikopar.repository.StokRepository;
import dev.fikopar.service.criteria.StokCriteria;
import dev.fikopar.service.dto.StokDTO;
import dev.fikopar.service.mapper.StokMapper;
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
 * Service for executing complex queries for {@link Stok} entities in the database.
 * The main input is a {@link StokCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link StokDTO} or a {@link Page} of {@link StokDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class StokQueryService extends QueryService<Stok> {

    private final Logger log = LoggerFactory.getLogger(StokQueryService.class);

    private final StokRepository stokRepository;

    private final StokMapper stokMapper;

    public StokQueryService(StokRepository stokRepository, StokMapper stokMapper) {
        this.stokRepository = stokRepository;
        this.stokMapper = stokMapper;
    }

    /**
     * Return a {@link List} of {@link StokDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<StokDTO> findByCriteria(StokCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Stok> specification = createSpecification(criteria);
        return stokMapper.toDto(stokRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link StokDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<StokDTO> findByCriteria(StokCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Stok> specification = createSpecification(criteria);
        return stokRepository.findAll(specification, page).map(stokMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(StokCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Stok> specification = createSpecification(criteria);
        return stokRepository.count(specification);
    }

    /**
     * Function to convert {@link StokCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Stok> createSpecification(StokCriteria criteria) {
        Specification<Stok> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), Stok_.id));
            }
            if (criteria.getStokno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getStokno(), Stok_.stokno));
            }
            if (criteria.getParcano() != null) {
                specification = specification.or(buildStringSpecification(criteria.getParcano(), Stok_.parcano));
            }
            if (criteria.getTipi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getTipi(), Stok_.tipi));
            }
            if (criteria.getCinsi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getCinsi(), Stok_.cinsi));
            }
            if (criteria.getMarka() != null) {
                specification = specification.or(buildStringSpecification(criteria.getMarka(), Stok_.marka));
            }
            if (criteria.getFiyati() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getFiyati(), Stok_.fiyati));
            }
            if (criteria.getKatalogId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getKatalogId(), root -> root.join(Stok_.katalog, JoinType.LEFT).get(Katalog_.id))
                    );
            }
        }
        return specification;
    }
}
