package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.repository.CariDevirRepository;
import com.dizel.fikopar.service.CariDevirQueryService;
import com.dizel.fikopar.service.CariDevirService;
import com.dizel.fikopar.service.criteria.CariDevirCriteria;
import com.dizel.fikopar.service.dto.CariDevirDTO;
import com.dizel.fikopar.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
 * REST controller for managing {@link com.dizel.fikopar.domain.CariDevir}.
 */
@RestController
@RequestMapping("/api")
public class CariDevirResource {

    private final Logger log = LoggerFactory.getLogger(CariDevirResource.class);

    private static final String ENTITY_NAME = "cariDevir";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CariDevirService cariDevirService;

    private final CariDevirRepository cariDevirRepository;

    private final CariDevirQueryService cariDevirQueryService;

    public CariDevirResource(
        CariDevirService cariDevirService,
        CariDevirRepository cariDevirRepository,
        CariDevirQueryService cariDevirQueryService
    ) {
        this.cariDevirService = cariDevirService;
        this.cariDevirRepository = cariDevirRepository;
        this.cariDevirQueryService = cariDevirQueryService;
    }

    /**
     * {@code POST  /cari-devirs} : Create a new cariDevir.
     *
     * @param cariDevirDTO the cariDevirDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cariDevirDTO, or with status {@code 400 (Bad Request)} if the cariDevir has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/cari-devirs")
    public ResponseEntity<CariDevirDTO> createCariDevir(@Valid @RequestBody CariDevirDTO cariDevirDTO) throws URISyntaxException {
        log.debug("REST request to save CariDevir : {}", cariDevirDTO);
        if (cariDevirDTO.getId() != null) {
            throw new BadRequestAlertException("A new cariDevir cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CariDevirDTO result = cariDevirService.save(cariDevirDTO);
        return ResponseEntity
            .created(new URI("/api/cari-devirs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /cari-devirs/:id} : Updates an existing cariDevir.
     *
     * @param id           the id of the cariDevirDTO to save.
     * @param cariDevirDTO the cariDevirDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cariDevirDTO,
     * or with status {@code 400 (Bad Request)} if the cariDevirDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cariDevirDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/cari-devirs/{id}")
    public ResponseEntity<CariDevirDTO> updateCariDevir(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CariDevirDTO cariDevirDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CariDevir : {}, {}", id, cariDevirDTO);
        if (cariDevirDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cariDevirDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cariDevirRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CariDevirDTO result = cariDevirService.update(cariDevirDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cariDevirDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /cari-devirs/:id} : Partial updates given fields of an existing cariDevir, field will ignore if it is null
     *
     * @param id           the id of the cariDevirDTO to save.
     * @param cariDevirDTO the cariDevirDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cariDevirDTO,
     * or with status {@code 400 (Bad Request)} if the cariDevirDTO is not valid,
     * or with status {@code 404 (Not Found)} if the cariDevirDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the cariDevirDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/cari-devirs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CariDevirDTO> partialUpdateCariDevir(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CariDevirDTO cariDevirDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CariDevir partially : {}, {}", id, cariDevirDTO);
        if (cariDevirDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cariDevirDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cariDevirRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CariDevirDTO> result = cariDevirService.partialUpdate(cariDevirDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cariDevirDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /cari-devirs} : get all the cariDevirs.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cariDevirs in body.
     */
    @GetMapping("/cari-devirs")
    public ResponseEntity<List<CariDevirDTO>> getAllCariDevirs(
        CariDevirCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CariDevirs by criteria: {}", criteria);
        Page<CariDevirDTO> page = cariDevirQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /cari-devirs/count} : count all the cariDevirs.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/cari-devirs/count")
    public ResponseEntity<Long> countCariDevirs(CariDevirCriteria criteria) {
        log.debug("REST request to count CariDevirs by criteria: {}", criteria);
        return ResponseEntity.ok().body(cariDevirQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /cari-devirs/:id} : get the "id" cariDevir.
     *
     * @param id the id of the cariDevirDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cariDevirDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/cari-devirs/{id}")
    public ResponseEntity<CariDevirDTO> getCariDevir(@PathVariable Long id) {
        log.debug("REST request to get CariDevir : {}", id);
        Optional<CariDevirDTO> cariDevirDTO = cariDevirService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cariDevirDTO);
    }

    /**
     * {@code DELETE  /cari-devirs/:id} : delete the "id" cariDevir.
     *
     * @param ids the id of the cariDevirDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/cari-devirs")
    public ResponseEntity<Void> deleteCariDevir(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete CariDevirs : {}", ids);
        ids.forEach(cariDevirService::delete);
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
