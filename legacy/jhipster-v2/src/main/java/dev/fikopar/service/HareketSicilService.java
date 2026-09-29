package dev.fikopar.service;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.service.dto.HareketSicilDTO;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link dev.fikopar.domain.HareketSicil}.
 */
public interface HareketSicilService {
    /**
     * Save a hareketSicil.
     *
     * @param hareketSicilDTO the entity to save.
     * @return the persisted entity.
     */
    HareketSicilDTO save(HareketSicilDTO hareketSicilDTO);

    /**
     * Updates a hareketSicil.
     *
     * @param hareketSicilDTO the entity to update.
     * @return the persisted entity.
     */
    HareketSicilDTO update(HareketSicilDTO hareketSicilDTO);

    /**
     * Partially updates a hareketSicil.
     *
     * @param hareketSicilDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<HareketSicilDTO> partialUpdate(HareketSicilDTO hareketSicilDTO);

    /**
     * Get all the hareketSicils.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<HareketSicilDTO> findAll(Pageable pageable);

    /**
     * Get the "id" hareketSicil.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<HareketSicilDTO> findOne(Long id);

    /**
     * Delete the "id" hareketSicil.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    String generateEvrakNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi);
}
