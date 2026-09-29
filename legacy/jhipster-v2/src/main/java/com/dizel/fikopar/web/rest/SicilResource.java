package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.repository.SicilRepository;
import com.dizel.fikopar.service.SicilQueryService;
import com.dizel.fikopar.service.SicilService;
import com.dizel.fikopar.service.criteria.SicilCriteria;
import com.dizel.fikopar.service.dto.SicilDTO;
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
 * REST controller for managing {@link com.dizel.fikopar.domain.Sicil}.
 */
@RestController
@RequestMapping("/api")
public class SicilResource {

    private final Logger log = LoggerFactory.getLogger(SicilResource.class);

    private static final String ENTITY_NAME = "sicil";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SicilService sicilService;

    private final SicilRepository sicilRepository;

    private final SicilQueryService sicilQueryService;

    public SicilResource(SicilService sicilService, SicilRepository sicilRepository, SicilQueryService sicilQueryService) {
        this.sicilService = sicilService;
        this.sicilRepository = sicilRepository;
        this.sicilQueryService = sicilQueryService;
    }

    /**
     * {@code POST  /sicils} : Create a new sicil.
     *
     * @param sicilDTO the sicilDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sicilDTO, or with status {@code 400 (Bad Request)} if the sicil has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/sicils")
    public ResponseEntity<SicilDTO> createSicil(@Valid @RequestBody SicilDTO sicilDTO) throws URISyntaxException {
        log.debug("REST request to save Sicil : {}", sicilDTO);
        if (sicilDTO.getId() != null) {
            throw new BadRequestAlertException("A new sicil cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SicilDTO result = sicilService.save(sicilDTO);
        return ResponseEntity
            .created(new URI("/api/sicils/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /sicils/:id} : Updates an existing sicil.
     *
     * @param id       the id of the sicilDTO to save.
     * @param sicilDTO the sicilDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sicilDTO,
     * or with status {@code 400 (Bad Request)} if the sicilDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sicilDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/sicils/{id}")
    public ResponseEntity<SicilDTO> updateSicil(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SicilDTO sicilDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Sicil : {}, {}", id, sicilDTO);
        if (sicilDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sicilDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sicilRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SicilDTO result = sicilService.update(sicilDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sicilDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /sicils/:id} : Partial updates given fields of an existing sicil, field will ignore if it is null
     *
     * @param id       the id of the sicilDTO to save.
     * @param sicilDTO the sicilDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sicilDTO,
     * or with status {@code 400 (Bad Request)} if the sicilDTO is not valid,
     * or with status {@code 404 (Not Found)} if the sicilDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the sicilDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/sicils/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SicilDTO> partialUpdateSicil(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SicilDTO sicilDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Sicil partially : {}, {}", id, sicilDTO);
        if (sicilDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sicilDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sicilRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SicilDTO> result = sicilService.partialUpdate(sicilDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sicilDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /sicils} : get all the sicils.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sicils in body.
     */
    @GetMapping("/sicils")
    public ResponseEntity<List<SicilDTO>> getAllSicils(
        SicilCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Sicils by criteria: {}", criteria);
        Page<SicilDTO> page = sicilQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /sicils/count} : count all the sicils.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/sicils/count")
    public ResponseEntity<Long> countSicils(SicilCriteria criteria) {
        log.debug("REST request to count Sicils by criteria: {}", criteria);
        return ResponseEntity.ok().body(sicilQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /sicils/:id} : get the "id" sicil.
     *
     * @param id the id of the sicilDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sicilDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/sicils/{id}")
    public ResponseEntity<SicilDTO> getSicil(@PathVariable Long id) {
        log.debug("REST request to get Sicil : {}", id);
        Optional<SicilDTO> sicilDTO = sicilService.findOne(id);
        return ResponseUtil.wrapOrNotFound(sicilDTO);
    }

    /**
     * {@code DELETE  /sicils/:id} : delete the "id" sicil.
     *
     * @param ids the id of the sicilDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/sicils")
    public ResponseEntity<Void> deleteSicils(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete Sicils : {}", ids);
        ids.forEach(sicilService::delete);
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
