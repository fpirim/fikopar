package com.dizel.fikopar.service;

import com.dizel.fikopar.domain.*; // for static metamodels
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.repository.SicilRepository;
import com.dizel.fikopar.service.criteria.SicilCriteria;
import com.dizel.fikopar.service.dto.SicilDTO;
import com.dizel.fikopar.service.mapper.SicilMapper;
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
 * Service for executing complex queries for {@link Sicil} entities in the database.
 * The main input is a {@link SicilCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SicilDTO} or a {@link Page} of {@link SicilDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SicilQueryService extends QueryService<Sicil> {

    private final Logger log = LoggerFactory.getLogger(SicilQueryService.class);

    private final SicilRepository sicilRepository;

    private final SicilMapper sicilMapper;

    public SicilQueryService(SicilRepository sicilRepository, SicilMapper sicilMapper) {
        this.sicilRepository = sicilRepository;
        this.sicilMapper = sicilMapper;
    }

    /**
     * Return a {@link List} of {@link SicilDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<SicilDTO> findByCriteria(SicilCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Sicil> specification = createSpecification(criteria);
        return sicilMapper.toDto(sicilRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link SicilDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SicilDTO> findByCriteria(SicilCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Sicil> specification = createSpecification(criteria);
        return sicilRepository.findAll(specification, page).map(sicilMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SicilCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Sicil> specification = createSpecification(criteria);
        return sicilRepository.count(specification);
    }

    /**
     * Function to convert {@link SicilCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Sicil> createSpecification(SicilCriteria criteria) {
        Specification<Sicil> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), Sicil_.id));
            }
            if (criteria.getHesapno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getHesapno(), Sicil_.hesapno));
            }
            if (criteria.getHesapadi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getHesapadi(), Sicil_.hesapadi));
            }
            if (criteria.getAdresi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getAdresi(), Sicil_.adresi));
            }
            if (criteria.getIlce() != null) {
                specification = specification.or(buildStringSpecification(criteria.getIlce(), Sicil_.ilce));
            }
            if (criteria.getSehir() != null) {
                specification = specification.or(buildStringSpecification(criteria.getSehir(), Sicil_.sehir));
            }
            if (criteria.getTelefon() != null) {
                specification = specification.or(buildStringSpecification(criteria.getTelefon(), Sicil_.telefon));
            }
            if (criteria.getVda() != null) {
                specification = specification.or(buildStringSpecification(criteria.getVda(), Sicil_.vda));
            }
            if (criteria.getVno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getVno(), Sicil_.vno));
            }
            if (criteria.getTcno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getTcno(), Sicil_.tcno));
            }
            if (criteria.getGun() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getGun(), Sicil_.gun));
            }
            if (criteria.getIskonto() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getIskonto(), Sicil_.iskonto));
            }
            if (criteria.getSiciltipi() != null) {
                specification = specification.or(buildSpecification(criteria.getSiciltipi(), Sicil_.siciltipi));
            }
            if (criteria.getCarisId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getCarisId(), root -> root.join(Sicil_.carises, JoinType.LEFT).get(Cari_.id))
                    );
            }
        }
        return specification;
    }
}
