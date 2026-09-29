package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.repository.StokRepository;
import com.dizel.fikopar.service.StokQueryService;
import com.dizel.fikopar.service.StokService;
import com.dizel.fikopar.service.criteria.StokCriteria;
import com.dizel.fikopar.service.dto.StokDTO;
import com.dizel.fikopar.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.dizel.fikopar.domain.Stok}.
 */
@RestController
@RequestMapping("/api")
public class StokResource {

    private final Logger log = LoggerFactory.getLogger(StokResource.class);

    private static final String ENTITY_NAME = "stok";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final StokService stokService;

    private final StokRepository stokRepository;

    private final StokQueryService stokQueryService;

    public StokResource(StokService stokService, StokRepository stokRepository, StokQueryService stokQueryService) {
        this.stokService = stokService;
        this.stokRepository = stokRepository;
        this.stokQueryService = stokQueryService;
    }

    /**
     * {@code POST  /stoks} : Create a new stok.
     *
     * @param stokDTO the stokDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new stokDTO, or with status {@code 400 (Bad Request)} if the stok has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/stoks")
    public ResponseEntity<StokDTO> createStok(@RequestBody StokDTO stokDTO) throws URISyntaxException {
        log.debug("REST request to save Stok : {}", stokDTO);
        if (stokDTO.getId() != null) {
            throw new BadRequestAlertException("A new stok cannot already have an ID", ENTITY_NAME, "idexists");
        }
        StokDTO result = stokService.save(stokDTO);
        return ResponseEntity
            .created(new URI("/api/stoks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /stoks/:id} : Updates an existing stok.
     *
     * @param id      the id of the stokDTO to save.
     * @param stokDTO the stokDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated stokDTO,
     * or with status {@code 400 (Bad Request)} if the stokDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the stokDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/stoks/{id}")
    public ResponseEntity<StokDTO> updateStok(@PathVariable(value = "id", required = false) final Long id, @RequestBody StokDTO stokDTO)
        throws URISyntaxException {
        log.debug("REST request to update Stok : {}, {}", id, stokDTO);
        if (stokDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, stokDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!stokRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        StokDTO result = stokService.update(stokDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, stokDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /stoks/:id} : Partial updates given fields of an existing stok, field will ignore if it is null
     *
     * @param id      the id of the stokDTO to save.
     * @param stokDTO the stokDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated stokDTO,
     * or with status {@code 400 (Bad Request)} if the stokDTO is not valid,
     * or with status {@code 404 (Not Found)} if the stokDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the stokDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/stoks/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<StokDTO> partialUpdateStok(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody StokDTO stokDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Stok partially : {}, {}", id, stokDTO);
        if (stokDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, stokDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!stokRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<StokDTO> result = stokService.partialUpdate(stokDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, stokDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /stoks} : get all the stoks.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of stoks in body.
     */
    @GetMapping("/stoks")
    public ResponseEntity<List<StokDTO>> getAllStoks(
        StokCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Stoks by criteria: {}", criteria);
        Page<StokDTO> page = stokQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /stoks/count} : count all the stoks.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/stoks/count")
    public ResponseEntity<Long> countStoks(StokCriteria criteria) {
        log.debug("REST request to count Stoks by criteria: {}", criteria);
        return ResponseEntity.ok().body(stokQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /stoks/:id} : get the "id" stok.
     *
     * @param id the id of the stokDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the stokDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/stoks/{id}")
    public ResponseEntity<StokDTO> getStok(@PathVariable Long id) {
        log.debug("REST request to get Stok : {}", id);
        Optional<StokDTO> stokDTO = stokService.findOne(id);
        return ResponseUtil.wrapOrNotFound(stokDTO);
    }

    /**
     * {@code DELETE  /stoks/:id} : delete the "id" stok.
     *
     * @param ids the id of the stokDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/stoks")
    public ResponseEntity<Void> deleteStoks(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete Stoks : {}", ids);
        ids.forEach(stokService::delete);
        return ResponseEntity
            .noContent()
            .headers(
                HeaderUtil.createEntityDeletionAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    ids.stream().map(String::valueOf).collect(Collectors.joining(","))
                )
            )
            .build();
    }
}
