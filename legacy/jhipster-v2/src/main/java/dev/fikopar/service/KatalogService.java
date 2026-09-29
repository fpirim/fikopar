package dev.fikopar.service;

import dev.fikopar.service.dto.KatalogDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link dev.fikopar.domain.Katalog}.
 */
public interface KatalogService {
    /**
     * Save a katalog.
     *
     * @param katalogDTO the entity to save.
     * @return the persisted entity.
     */
    KatalogDTO save(KatalogDTO katalogDTO);

    /**
     * Updates a katalog.
     *
     * @param katalogDTO the entity to update.
     * @return the persisted entity.
     */
    KatalogDTO update(KatalogDTO katalogDTO);

    /**
     * Partially updates a katalog.
     *
     * @param katalogDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<KatalogDTO> partialUpdate(KatalogDTO katalogDTO);

    /**
     * Get all the katalogs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<KatalogDTO> findAll(Pageable pageable);

    /**
     * Get the "id" katalog.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<KatalogDTO> findOne(Long id);

    /**
     * Delete the "id" katalog.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
