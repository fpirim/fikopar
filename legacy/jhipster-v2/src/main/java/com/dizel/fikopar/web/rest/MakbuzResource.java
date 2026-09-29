package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.MakbuzRepository;
import com.dizel.fikopar.service.MakbuzQueryService;
import com.dizel.fikopar.service.MakbuzService;
import com.dizel.fikopar.service.criteria.MakbuzCriteria;
import com.dizel.fikopar.service.dto.MakbuzDTO;
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
 * REST controller for managing {@link com.dizel.fikopar.domain.Makbuz}.
 */
@RestController
@RequestMapping("/api")
public class MakbuzResource {

    private final Logger log = LoggerFactory.getLogger(MakbuzResource.class);

    private static final String ENTITY_NAME = "makbuz";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MakbuzService makbuzService;

    private final MakbuzRepository makbuzRepository;

    private final MakbuzQueryService makbuzQueryService;

    public MakbuzResource(MakbuzService makbuzService, MakbuzRepository makbuzRepository, MakbuzQueryService makbuzQueryService) {
        this.makbuzService = makbuzService;
        this.makbuzRepository = makbuzRepository;
        this.makbuzQueryService = makbuzQueryService;
    }

    /**
     * {@code POST  /makbuzs} : Create a new makbuz.
     *
     * @param makbuzDTO the makbuzDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new makbuzDTO, or with status {@code 400 (Bad Request)} if the makbuz has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/makbuzs")
    public ResponseEntity<MakbuzDTO> createMakbuz(@Valid @RequestBody MakbuzDTO makbuzDTO) throws URISyntaxException {
        log.debug("REST request to save Makbuz : {}", makbuzDTO);
        if (makbuzDTO.getId() != null) {
            throw new BadRequestAlertException("A new makbuz cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MakbuzDTO result = makbuzService.save(makbuzDTO);
        return ResponseEntity
            .created(new URI("/api/makbuzs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /makbuzs/:id} : Updates an existing makbuz.
     *
     * @param id        the id of the makbuzDTO to save.
     * @param makbuzDTO the makbuzDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated makbuzDTO,
     * or with status {@code 400 (Bad Request)} if the makbuzDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the makbuzDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/makbuzs/{id}")
    public ResponseEntity<MakbuzDTO> updateMakbuz(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MakbuzDTO makbuzDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Makbuz : {}, {}", id, makbuzDTO);
        if (makbuzDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, makbuzDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!makbuzRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MakbuzDTO result = makbuzService.update(makbuzDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, makbuzDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /makbuzs/:id} : Partial updates given fields of an existing makbuz, field will ignore if it is null
     *
     * @param id        the id of the makbuzDTO to save.
     * @param makbuzDTO the makbuzDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated makbuzDTO,
     * or with status {@code 400 (Bad Request)} if the makbuzDTO is not valid,
     * or with status {@code 404 (Not Found)} if the makbuzDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the makbuzDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/makbuzs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MakbuzDTO> partialUpdateMakbuz(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MakbuzDTO makbuzDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Makbuz partially : {}, {}", id, makbuzDTO);
        if (makbuzDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, makbuzDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!makbuzRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MakbuzDTO> result = makbuzService.partialUpdate(makbuzDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, makbuzDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /makbuzs} : get all the makbuzs.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of makbuzs in body.
     */
    @GetMapping("/makbuzs")
    public ResponseEntity<List<MakbuzDTO>> getAllMakbuzs(
        MakbuzCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Makbuzs by criteria: {}", criteria);
        Page<MakbuzDTO> page = makbuzQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /makbuzs/count} : count all the makbuzs.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/makbuzs/count")
    public ResponseEntity<Long> countMakbuzs(MakbuzCriteria criteria) {
        log.debug("REST request to count Makbuzs by criteria: {}", criteria);
        return ResponseEntity.ok().body(makbuzQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /makbuzs/:id} : get the "id" makbuz.
     *
     * @param id the id of the makbuzDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the makbuzDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/makbuzs/{id}")
    public ResponseEntity<MakbuzDTO> getMakbuz(@PathVariable Long id) {
        log.debug("REST request to get Makbuz : {}", id);
        Optional<MakbuzDTO> makbuzDTO = makbuzService.findOne(id);
        return ResponseUtil.wrapOrNotFound(makbuzDTO);
    }

    /**
     * {@code DELETE  /makbuzs/:id} : delete the "id" makbuz.
     *
     * @param ids the id of the makbuzDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/makbuzs")
    public ResponseEntity<Void> deleteMakbuz(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete Makbuzs : {}", ids);
        ids.forEach(makbuzService::delete);
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

    @GetMapping("/makbuzs/generate")
    public ResponseEntity<String> generateMakbuzNo(
        @RequestParam(value = "evraktipi", required = false) EvrakTipi evrakTipi,
        @RequestParam(value = "depotipi", required = false) DepoTipi depoTipi,
        @RequestParam(value = "tarihi", required = false) LocalDate tarihi
    ) {
        return ResponseEntity.ok(makbuzService.generateMakbuzNo(evrakTipi, depoTipi, tarihi));
    }
}
