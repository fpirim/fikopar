package dev.fikopar.service;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.service.dto.MakbuzDTO;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link dev.fikopar.domain.Makbuz}.
 */
public interface MakbuzService {
    void createOrUpdateCari(MakbuzDTO makbuzDTO);
    /**
     * Save a makbuz.
     *
     * @param makbuzDTO the entity to save.
     * @return the persisted entity.
     */
    MakbuzDTO save(MakbuzDTO makbuzDTO);

    /**
     * Updates a makbuz.
     *
     * @param makbuzDTO the entity to update.
     * @return the persisted entity.
     */
    MakbuzDTO update(MakbuzDTO makbuzDTO);

    /**
     * Partially updates a makbuz.
     *
     * @param makbuzDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MakbuzDTO> partialUpdate(MakbuzDTO makbuzDTO);

    /**
     * Get all the makbuzs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MakbuzDTO> findAll(Pageable pageable);

    /**
     * Get the "id" makbuz.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MakbuzDTO> findOne(Long id);

    /**
     * Delete the "id" makbuz.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    String generateMakbuzNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi);
}
