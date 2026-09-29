package dev.fikopar.service;

import dev.fikopar.domain.*; // for static metamodels
import dev.fikopar.domain.HareketSicil;
import dev.fikopar.repository.HareketSicilRepository;
import dev.fikopar.service.criteria.HareketSicilCriteria;
import dev.fikopar.service.dto.HareketSicilDTO;
import dev.fikopar.service.mapper.HareketSicilMapper;
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
 * Service for executing complex queries for {@link HareketSicil} entities in the database.
 * The main input is a {@link HareketSicilCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link HareketSicilDTO} or a {@link Page} of {@link HareketSicilDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class HareketSicilQueryService extends QueryService<HareketSicil> {

    private final Logger log = LoggerFactory.getLogger(HareketSicilQueryService.class);

    private final HareketSicilRepository hareketSicilRepository;

    private final HareketSicilMapper hareketSicilMapper;

    public HareketSicilQueryService(HareketSicilRepository hareketSicilRepository, HareketSicilMapper hareketSicilMapper) {
        this.hareketSicilRepository = hareketSicilRepository;
        this.hareketSicilMapper = hareketSicilMapper;
    }

    /**
     * Return a {@link List} of {@link HareketSicilDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<HareketSicilDTO> findByCriteria(HareketSicilCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<HareketSicil> specification = createSpecification(criteria);
        return hareketSicilMapper.toDto(hareketSicilRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link HareketSicilDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<HareketSicilDTO> findByCriteria(HareketSicilCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<HareketSicil> specification = createSpecification(criteria);
        return hareketSicilRepository.findAll(specification, page).map(hareketSicilMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(HareketSicilCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<HareketSicil> specification = createSpecification(criteria);
        return hareketSicilRepository.count(specification);
    }

    /**
     * Function to convert {@link HareketSicilCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<HareketSicil> createSpecification(HareketSicilCriteria criteria) {
        Specification<HareketSicil> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getId(), HareketSicil_.id));
            }
            if (criteria.getEvrakno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getEvrakno(), HareketSicil_.evrakno));
            }
            if (criteria.getSevktarihi() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getSevktarihi(), HareketSicil_.sevktarihi));
            }
            if (criteria.getSaati() != null) {
                specification = specification.or(buildStringSpecification(criteria.getSaati(), HareketSicil_.saati));
            }
            if (criteria.getHesapno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getHesapno(), HareketSicil_.hesapno));
            }
            if (criteria.getHesapadi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getHesapadi(), HareketSicil_.hesapadi));
            }
            if (criteria.getAdresi() != null) {
                specification = specification.or(buildStringSpecification(criteria.getAdresi(), HareketSicil_.adresi));
            }
            if (criteria.getIlce() != null) {
                specification = specification.or(buildStringSpecification(criteria.getIlce(), HareketSicil_.ilce));
            }
            if (criteria.getSehir() != null) {
                specification = specification.or(buildStringSpecification(criteria.getSehir(), HareketSicil_.sehir));
            }
            if (criteria.getVda() != null) {
                specification = specification.or(buildStringSpecification(criteria.getVda(), HareketSicil_.vda));
            }
            if (criteria.getTcno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getTcno(), HareketSicil_.tcno));
            }
            if (criteria.getVno() != null) {
                specification = specification.or(buildStringSpecification(criteria.getVno(), HareketSicil_.vno));
            }
            if (criteria.getTelefon() != null) {
                specification = specification.or(buildStringSpecification(criteria.getTelefon(), HareketSicil_.telefon));
            }
            if (criteria.getGun() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getGun(), HareketSicil_.gun));
            }
            if (criteria.getTutari() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getTutari(), HareketSicil_.tutari));
            }
            if (criteria.getNotlar() != null) {
                specification = specification.or(buildStringSpecification(criteria.getNotlar(), HareketSicil_.notlar));
            }
            if (criteria.getDepotipi() != null) {
                specification = specification.or(buildSpecification(criteria.getDepotipi(), HareketSicil_.depotipi));
            }
            if (criteria.getToplam() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getToplam(), HareketSicil_.toplam));
            }
            if (criteria.getIsktoplam() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getIsktoplam(), HareketSicil_.isktoplam));
            }
            if (criteria.getKdvtoplam() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getKdvtoplam(), HareketSicil_.kdvtoplam));
            }
            if (criteria.getAratoplam() != null) {
                specification = specification.or(buildRangeSpecification(criteria.getAratoplam(), HareketSicil_.aratoplam));
            }
            if (criteria.getSicilId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getSicilId(), root -> root.join(HareketSicil_.sicil, JoinType.LEFT).get(Sicil_.id))
                    );
            }
            if (criteria.getCariId() != null) {
                specification =
                    specification.or(
                        buildSpecification(criteria.getCariId(), root -> root.join(HareketSicil_.cari, JoinType.LEFT).get(Cari_.id))
                    );
            }
            if (criteria.getHareketDetaysId() != null) {
                specification =
                    specification.or(
                        buildSpecification(
                            criteria.getHareketDetaysId(),
                            root -> root.join(HareketSicil_.hareketDetays, JoinType.LEFT).get(HareketDetay_.id)
                        )
                    );
            }
            if (criteria.getEvraktipi() != null) {
                specification = specification.and(buildSpecification(criteria.getEvraktipi(), HareketSicil_.evraktipi));
            }
            if (criteria.getTarihi() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTarihi(), HareketSicil_.tarihi));
            }
        }
        return specification;
    }
}
