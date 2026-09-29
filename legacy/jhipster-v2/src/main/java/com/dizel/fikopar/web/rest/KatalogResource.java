package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.repository.KatalogRepository;
import com.dizel.fikopar.service.KatalogQueryService;
import com.dizel.fikopar.service.KatalogService;
import com.dizel.fikopar.service.criteria.KatalogCriteria;
import com.dizel.fikopar.service.dto.KatalogDTO;
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
 * REST controller for managing {@link com.dizel.fikopar.domain.Katalog}.
 */
@RestController
@RequestMapping("/api")
public class KatalogResource {

    private final Logger log = LoggerFactory.getLogger(KatalogResource.class);

    private static final String ENTITY_NAME = "katalog";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final KatalogService katalogService;

    private final KatalogRepository katalogRepository;

    private final KatalogQueryService katalogQueryService;

    public KatalogResource(KatalogService katalogService, KatalogRepository katalogRepository, KatalogQueryService katalogQueryService) {
        this.katalogService = katalogService;
        this.katalogRepository = katalogRepository;
        this.katalogQueryService = katalogQueryService;
    }

    /**
     * {@code POST  /katalogs} : Create a new katalog.
     *
     * @param katalogDTO the katalogDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new katalogDTO, or with status {@code 400 (Bad Request)} if the katalog has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/katalogs")
    public ResponseEntity<KatalogDTO> createKatalog(@RequestBody KatalogDTO katalogDTO) throws URISyntaxException {
        log.debug("REST request to save Katalog : {}", katalogDTO);
        if (katalogDTO.getId() != null) {
            throw new BadRequestAlertException("A new katalog cannot already have an ID", ENTITY_NAME, "idexists");
        }
        KatalogDTO result = katalogService.save(katalogDTO);
        return ResponseEntity
            .created(new URI("/api/katalogs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /katalogs/:id} : Updates an existing katalog.
     *
     * @param id         the id of the katalogDTO to save.
     * @param katalogDTO the katalogDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated katalogDTO,
     * or with status {@code 400 (Bad Request)} if the katalogDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the katalogDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/katalogs/{id}")
    public ResponseEntity<KatalogDTO> updateKatalog(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody KatalogDTO katalogDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Katalog : {}, {}", id, katalogDTO);
        if (katalogDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, katalogDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!katalogRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        KatalogDTO result = katalogService.update(katalogDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, katalogDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /katalogs/:id} : Partial updates given fields of an existing katalog, field will ignore if it is null
     *
     * @param id         the id of the katalogDTO to save.
     * @param katalogDTO the katalogDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated katalogDTO,
     * or with status {@code 400 (Bad Request)} if the katalogDTO is not valid,
     * or with status {@code 404 (Not Found)} if the katalogDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the katalogDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/katalogs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<KatalogDTO> partialUpdateKatalog(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody KatalogDTO katalogDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Katalog partially : {}, {}", id, katalogDTO);
        if (katalogDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, katalogDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!katalogRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<KatalogDTO> result = katalogService.partialUpdate(katalogDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, katalogDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /katalogs} : get all the katalogs.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of katalogs in body.
     */
    @GetMapping("/katalogs")
    public ResponseEntity<List<KatalogDTO>> getAllKatalogs(
        KatalogCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Katalogs by criteria: {}", criteria);
        Page<KatalogDTO> page = katalogQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /katalogs/count} : count all the katalogs.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/katalogs/count")
    public ResponseEntity<Long> countKatalogs(KatalogCriteria criteria) {
        log.debug("REST request to count Katalogs by criteria: {}", criteria);
        return ResponseEntity.ok().body(katalogQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /katalogs/:id} : get the "id" katalog.
     *
     * @param id the id of the katalogDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the katalogDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/katalogs/{id}")
    public ResponseEntity<KatalogDTO> getKatalog(@PathVariable Long id) {
        log.debug("REST request to get Katalog : {}", id);
        Optional<KatalogDTO> katalogDTO = katalogService.findOne(id);
        return ResponseUtil.wrapOrNotFound(katalogDTO);
    }

    /**
     * {@code DELETE  /katalogs/:id} : delete the "id" katalog.
     *
     * @param ids the id of the katalogDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/katalogs")
    public ResponseEntity<Void> deleteKatalogs(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete Katalogs : {}", ids);
        ids.forEach(katalogService::delete);
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
