package dev.fikopar.service;

import dev.fikopar.service.dto.CariDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link dev.fikopar.domain.Cari}.
 */
public interface CariService {
    /**
     * Save a cari.
     *
     * @param cariDTO the entity to save.
     * @return the persisted entity.
     */
    CariDTO save(CariDTO cariDTO);

    /**
     * Updates a cari.
     *
     * @param cariDTO the entity to update.
     * @return the persisted entity.
     */
    CariDTO update(CariDTO cariDTO);

    /**
     * Partially updates a cari.
     *
     * @param cariDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CariDTO> partialUpdate(CariDTO cariDTO);

    /**
     * Get all the caris.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CariDTO> findAll(Pageable pageable);

    /**
     * Get the "id" cari.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CariDTO> findOne(Long id);

    /**
     * Delete the "id" cari.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
