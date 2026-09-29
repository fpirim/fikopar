package com.dizel.fikopar.service;

import com.dizel.fikopar.service.dto.CariDevirDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.dizel.fikopar.domain.CariDevir}.
 */
public interface CariDevirService {
    /**
     * Save a cariDevir.
     *
     * @param cariDevirDTO the entity to save.
     * @return the persisted entity.
     */
    CariDevirDTO save(CariDevirDTO cariDevirDTO);

    /**
     * Updates a cariDevir.
     *
     * @param cariDevirDTO the entity to update.
     * @return the persisted entity.
     */
    CariDevirDTO update(CariDevirDTO cariDevirDTO);

    /**
     * Partially updates a cariDevir.
     *
     * @param cariDevirDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CariDevirDTO> partialUpdate(CariDevirDTO cariDevirDTO);

    /**
     * Get all the cariDevirs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CariDevirDTO> findAll(Pageable pageable);

    /**
     * Get the "id" cariDevir.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CariDevirDTO> findOne(Long id);

    /**
     * Delete the "id" cariDevir.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
