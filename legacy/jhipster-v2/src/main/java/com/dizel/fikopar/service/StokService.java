package com.dizel.fikopar.service;

import com.dizel.fikopar.service.dto.StokDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.dizel.fikopar.domain.Stok}.
 */
public interface StokService {
    /**
     * Save a stok.
     *
     * @param stokDTO the entity to save.
     * @return the persisted entity.
     */
    StokDTO save(StokDTO stokDTO);

    /**
     * Updates a stok.
     *
     * @param stokDTO the entity to update.
     * @return the persisted entity.
     */
    StokDTO update(StokDTO stokDTO);

    /**
     * Partially updates a stok.
     *
     * @param stokDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<StokDTO> partialUpdate(StokDTO stokDTO);

    /**
     * Get all the stoks.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<StokDTO> findAll(Pageable pageable);

    /**
     * Get the "id" stok.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<StokDTO> findOne(Long id);

    /**
     * Delete the "id" stok.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
