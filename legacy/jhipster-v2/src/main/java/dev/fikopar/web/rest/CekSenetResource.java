package dev.fikopar.web.rest;

import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.repository.CekSenetRepository;
import dev.fikopar.service.CekSenetQueryService;
import dev.fikopar.service.CekSenetService;
import dev.fikopar.service.criteria.CekSenetCriteria;
import dev.fikopar.service.dto.CekSenetDTO;
import dev.fikopar.web.rest.errors.BadRequestAlertException;
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
 * REST controller for managing {@link dev.fikopar.domain.CekSenet}.
 */
@RestController
@RequestMapping("/api")
public class CekSenetResource {

    private final Logger log = LoggerFactory.getLogger(CekSenetResource.class);

    private static final String ENTITY_NAME = "cekSenet";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CekSenetService cekSenetService;

    private final CekSenetRepository cekSenetRepository;

    private final CekSenetQueryService cekSenetQueryService;

    public CekSenetResource(
        CekSenetService cekSenetService,
        CekSenetRepository cekSenetRepository,
        CekSenetQueryService cekSenetQueryService
    ) {
        this.cekSenetService = cekSenetService;
        this.cekSenetRepository = cekSenetRepository;
        this.cekSenetQueryService = cekSenetQueryService;
    }

    /**
     * {@code POST  /cek-senets} : Create a new cekSenet.
     *
     * @param cekSenetDTO the cekSenetDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cekSenetDTO, or with status {@code 400 (Bad Request)} if the cekSenet has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/cek-senets")
    public ResponseEntity<CekSenetDTO> createCekSenet(@Valid @RequestBody CekSenetDTO cekSenetDTO) throws URISyntaxException {
        log.debug("REST request to save CekSenet : {}", cekSenetDTO);
        if (cekSenetDTO.getId() != null) {
            throw new BadRequestAlertException("A new cekSenet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CekSenetDTO result = cekSenetService.save(cekSenetDTO);
        return ResponseEntity
            .created(new URI("/api/cek-senets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /cek-senets/:id} : Updates an existing cekSenet.
     *
     * @param id          the id of the cekSenetDTO to save.
     * @param cekSenetDTO the cekSenetDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cekSenetDTO,
     * or with status {@code 400 (Bad Request)} if the cekSenetDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cekSenetDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/cek-senets/{id}")
    public ResponseEntity<CekSenetDTO> updateCekSenet(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CekSenetDTO cekSenetDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CekSenet : {}, {}", id, cekSenetDTO);
        if (cekSenetDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cekSenetDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cekSenetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CekSenetDTO result = cekSenetService.update(cekSenetDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cekSenetDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /cek-senets/:id} : Partial updates given fields of an existing cekSenet, field will ignore if it is null
     *
     * @param id          the id of the cekSenetDTO to save.
     * @param cekSenetDTO the cekSenetDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cekSenetDTO,
     * or with status {@code 400 (Bad Request)} if the cekSenetDTO is not valid,
     * or with status {@code 404 (Not Found)} if the cekSenetDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the cekSenetDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/cek-senets/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CekSenetDTO> partialUpdateCekSenet(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CekSenetDTO cekSenetDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CekSenet partially : {}, {}", id, cekSenetDTO);
        if (cekSenetDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cekSenetDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cekSenetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CekSenetDTO> result = cekSenetService.partialUpdate(cekSenetDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cekSenetDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /cek-senets} : get all the cekSenets.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cekSenets in body.
     */
    @GetMapping("/cek-senets")
    public ResponseEntity<List<CekSenetDTO>> getAllCekSenets(
        CekSenetCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CekSenets by criteria: {}", criteria);
        Page<CekSenetDTO> page = cekSenetQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /cek-senets/count} : count all the cekSenets.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/cek-senets/count")
    public ResponseEntity<Long> countCekSenets(CekSenetCriteria criteria) {
        log.debug("REST request to count CekSenets by criteria: {}", criteria);
        return ResponseEntity.ok().body(cekSenetQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /cek-senets/:id} : get the "id" cekSenet.
     *
     * @param id the id of the cekSenetDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cekSenetDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/cek-senets/{id}")
    public ResponseEntity<CekSenetDTO> getCekSenet(@PathVariable Long id) {
        log.debug("REST request to get CekSenet : {}", id);
        Optional<CekSenetDTO> cekSenetDTO = cekSenetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cekSenetDTO);
    }

    /**
     * {@code DELETE  /cek-senets/:id} : delete the "id" cekSenet.
     *
     * @param ids the id of the cekSenetDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/cek-senets")
    public ResponseEntity<Void> deleteCekSenets(@RequestBody Set<Long> ids) {
        log.debug("REST request to delete CekSenets : {}", ids);
        ids.forEach(cekSenetService::delete);
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

    @GetMapping("/cek-senets/generate")
    public ResponseEntity<String> generateCekSenetNo(
        @RequestParam(value = "evraktipi", required = false) EvrakTipi evrakTipi,
        @RequestParam(value = "depotipi", required = false) DepoTipi depoTipi,
        @RequestParam(value = "tarihi", required = false) LocalDate tarihi
    ) {
        return ResponseEntity.ok(cekSenetService.generateCekSenetNo(evrakTipi, depoTipi, tarihi));
    }
}
