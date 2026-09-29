package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.HareketSicilRepository;
import com.dizel.fikopar.service.HareketSicilQueryService;
import com.dizel.fikopar.service.HareketSicilService;
import com.dizel.fikopar.service.criteria.HareketSicilCriteria;
import com.dizel.fikopar.service.dto.HareketSicilDTO;
import com.dizel.fikopar.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
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
 * REST controller for managing {@link com.dizel.fikopar.domain.HareketSicil}.
 */
@RestController
@RequestMapping("/api")
public class HareketSicilResource {

    private final Logger log = LoggerFactory.getLogger(HareketSicilResource.class);

    private static final String ENTITY_NAME = "hareketSicil";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final HareketSicilService hareketSicilService;

    private final HareketSicilRepository hareketSicilRepository;

    private final HareketSicilQueryService hareketSicilQueryService;

    public HareketSicilResource(
        HareketSicilService hareketSicilService,
        HareketSicilRepository hareketSicilRepository,
        HareketSicilQueryService hareketSicilQueryService
    ) {
        this.hareketSicilService = hareketSicilService;
        this.hareketSicilRepository = hareketSicilRepository;
        this.hareketSicilQueryService = hareketSicilQueryService;
    }

    /**
     * {@code POST  /hareket-sicils} : Create a new hareketSicil.
     *
     * @param hareketSicilDTO the hareketSicilDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new hareketSicilDTO, or with status {@code 400 (Bad Request)} if the hareketSicil has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/hareket-sicils")
    public ResponseEntity<HareketSicilDTO> createHareketSicil(@Valid @RequestBody HareketSicilDTO hareketSicilDTO)
        throws URISyntaxException {
        log.debug("REST request to save HareketSicil : {}", hareketSicilDTO);
        if (hareketSicilDTO.getId() != null) {
            throw new BadRequestAlertException("A new hareketSicil cannot already have an ID", ENTITY_NAME, "idexists");
        }
        HareketSicilDTO result = hareketSicilService.save(hareketSicilDTO);
        return ResponseEntity
            .created(new URI("/api/hareket-sicils/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /hareket-sicils/:id} : Updates an existing hareketSicil.
     *
     * @param id              the id of the hareketSicilDTO to save.
     * @param hareketSicilDTO the hareketSicilDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hareketSicilDTO,
     * or with status {@code 400 (Bad Request)} if the hareketSicilDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the hareketSicilDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/hareket-sicils/{id}")
    public ResponseEntity<HareketSicilDTO> updateHareketSicil(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HareketSicilDTO hareketSicilDTO
    ) throws URISyntaxException {
        log.debug("REST request to update HareketSicil : {}, {}", id, hareketSicilDTO);
        if (hareketSicilDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hareketSicilDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hareketSicilRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        HareketSicilDTO result = hareketSicilService.update(hareketSicilDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, hareketSicilDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /hareket-sicils/:id} : Partial updates given fields of an existing hareketSicil, field will ignore if it is null
     *
     * @param id              the id of the hareketSicilDTO to save.
     * @param hareketSicilDTO the hareketSicilDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hareketSicilDTO,
     * or with status {@code 400 (Bad Request)} if the hareketSicilDTO is not valid,
     * or with status {@code 404 (Not Found)} if the hareketSicilDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the hareketSicilDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/hareket-sicils/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HareketSicilDTO> partialUpdateHareketSicil(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HareketSicilDTO hareketSicilDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update HareketSicil partially : {}, {}", id, hareketSicilDTO);
        if (hareketSicilDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hareketSicilDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hareketSicilRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HareketSicilDTO> result = hareketSicilService.partialUpdate(hareketSicilDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, hareketSicilDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /hareket-sicils} : get all the hareketSicils.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of hareketSicils in body.
     */
    @GetMapping("/hareket-sicils")
    public ResponseEntity<List<HareketSicilDTO>> getAllHareketSicils(
        HareketSicilCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get HareketSicils by criteria: {}", criteria);
        Page<HareketSicilDTO> page = hareketSicilQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /hareket-sicils/count} : count all the hareketSicils.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/hareket-sicils/count")
    public ResponseEntity<Long> countHareketSicils(HareketSicilCriteria criteria) {
        log.debug("REST request to count HareketSicils by criteria: {}", criteria);
        return ResponseEntity.ok().body(hareketSicilQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /hareket-sicils/:id} : get the "id" hareketSicil.
     *
     * @param id the id of the hareketSicilDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the hareketSicilDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/hareket-sicils/{id}")
    public ResponseEntity<HareketSicilDTO> getHareketSicil(@PathVariable Long id) {
        log.debug("REST request to get HareketSicil : {}", id);
        Optional<HareketSicilDTO> hareketSicilDTO = hareketSicilService.findOne(id);
        return ResponseUtil.wrapOrNotFound(hareketSicilDTO);
    }

    /**
     * {@code DELETE  /hareket-sicils : delete the "id" hareketSicil.
     *
     * @param ids the id of the hareketSicilDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/hareket-sicils")
    public ResponseEntity<Void> deleteHareketSicils(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete HareketSicils : {}", ids);
        ids.forEach(hareketSicilService::delete);
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

    @GetMapping("/hareket-sicils/generate")
    public ResponseEntity<String> generateEvrakNo(
        @RequestParam(value = "evraktipi", required = false) EvrakTipi evrakTipi,
        @RequestParam(value = "depotipi", required = false) DepoTipi depoTipi,
        @RequestParam(value = "tarihi", required = false) LocalDate tarihi
    ) {
        return ResponseEntity.ok(hareketSicilService.generateEvrakNo(evrakTipi, depoTipi, tarihi));
    }
}
