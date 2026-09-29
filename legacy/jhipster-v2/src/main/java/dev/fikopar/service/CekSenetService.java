package dev.fikopar.service;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.service.dto.CekSenetDTO;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link dev.fikopar.domain.CekSenet}.
 */
public interface CekSenetService {
    /**
     * Save a cekSenet.
     *
     * @param cekSenetDTO the entity to save.
     * @return the persisted entity.
     */
    CekSenetDTO save(CekSenetDTO cekSenetDTO);

    /**
     * Updates a cekSenet.
     *
     * @param cekSenetDTO the entity to update.
     * @return the persisted entity.
     */
    CekSenetDTO update(CekSenetDTO cekSenetDTO);

    /**
     * Partially updates a cekSenet.
     *
     * @param cekSenetDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CekSenetDTO> partialUpdate(CekSenetDTO cekSenetDTO);

    /**
     * Get all the cekSenets.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CekSenetDTO> findAll(Pageable pageable);

    /**
     * Get the "id" cekSenet.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CekSenetDTO> findOne(Long id);

    /**
     * Delete the "id" cekSenet.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    String generateCekSenetNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi);
}
