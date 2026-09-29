package dev.fikopar.service;

import dev.fikopar.domain.*; // for static metamodels
import dev.fikopar.domain.Makbuz;
import dev.fikopar.repository.MakbuzRepository;
import dev.fikopar.service.criteria.MakbuzCriteria;
import dev.fikopar.service.dto.MakbuzDTO;
import dev.fikopar.service.mapper.MakbuzMapper;
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
 * Service for executing complex queries for {@link Makbuz} entities in the database.
 * The main input is a {@link MakbuzCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MakbuzDTO} or a {@link Page} of {@link MakbuzDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MakbuzQueryService extends QueryService<Makbuz> {

    private final Logger log = LoggerFactory.getLogger(MakbuzQueryService.class);

    private final MakbuzRepository makbuzRepository;

    private final MakbuzMapper makbuzMapper;

    public MakbuzQueryService(MakbuzRepository makbuzRepository, MakbuzMapper makbuzMapper) {
        this.makbuzRepository = makbuzRepository;
        this.makbuzMapper = makbuzMapper;
    }

    /**
     * Return a {@link List} of {@link MakbuzDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<MakbuzDTO> findByCriteria(MakbuzCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Makbuz> specification = createSpecification(criteria);
        return makbuzMapper.toDto(makbuzRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link MakbuzDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MakbuzDTO> findByCriteria(MakbuzCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Makbuz> specification = createSpecification(criteria);
        return makbuzRepository.findAll(specification, page).map(makbuzMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MakbuzCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Makbuz> specification = createSpecification(criteria);
        return makbuzRepository.count(specification);
    }

    /**
     * Function to convert {@link MakbuzCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Makbuz> createSpecification(MakbuzCriteria criteria) {
        Specification<Makbuz> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), Makbuz_.id));
            }
            if (criteria.getMakbuzno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getMakbuzno(), Makbuz_.makbuzno));
            }
            if (criteria.getEvraktipi() != null) {
                specification = specification.or(buildSpecification(criteria.getEvraktipi(), Makbuz_.evraktipi));
            }
            if (criteria.getDepotipi() != null) {
                specification = specification.or(buildSpecification(criteria.getDepotipi(), Makbuz_.depotipi));
            }
            if (criteria.getTarihi() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getTarihi(), Makbuz_.tarihi));
            }
            if (criteria.getVade() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getVade(), Makbuz_.vade));
            }
            if (criteria.getAciklama() != null) {
                specification = specification.or(buildStringSpecification(criteria.getAciklama(), Makbuz_.aciklama));
            }
            if (criteria.getTutari() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getTutari(), Makbuz_.tutari));
            }
            if (criteria.getSicilId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getSicilId(), root -> root.join(Makbuz_.sicil, JoinType.LEFT).get(Sicil_.id))
                    );
            }
            if (criteria.getCariId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getCariId(), root -> root.join(Makbuz_.cari, JoinType.LEFT).get(Cari_.id))
                    );
            }
        }
        return specification;
    }
}
