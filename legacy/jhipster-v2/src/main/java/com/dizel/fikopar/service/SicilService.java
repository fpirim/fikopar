package com.dizel.fikopar.service;

import com.dizel.fikopar.service.dto.SicilDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.dizel.fikopar.domain.Sicil}.
 */
public interface SicilService {
    /**
     * Save a sicil.
     *
     * @param sicilDTO the entity to save.
     * @return the persisted entity.
     */
    SicilDTO save(SicilDTO sicilDTO);

    /**
     * Updates a sicil.
     *
     * @param sicilDTO the entity to update.
     * @return the persisted entity.
     */
    SicilDTO update(SicilDTO sicilDTO);

    /**
     * Partially updates a sicil.
     *
     * @param sicilDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SicilDTO> partialUpdate(SicilDTO sicilDTO);

    /**
     * Get all the sicils.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SicilDTO> findAll(Pageable pageable);

    /**
     * Get the "id" sicil.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SicilDTO> findOne(Long id);

    /**
     * Delete the "id" sicil.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
