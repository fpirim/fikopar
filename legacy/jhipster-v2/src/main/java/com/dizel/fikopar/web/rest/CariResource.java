package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.service.CariQueryService;
import com.dizel.fikopar.service.criteria.CariCriteria;
import com.dizel.fikopar.service.dto.CariDTO;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing {@link com.dizel.fikopar.domain.Cari}.
 */
@RestController
@RequestMapping("/api")
public class CariResource {

    private final Logger log = LoggerFactory.getLogger(CariResource.class);

    //    private static final String ENTITY_NAME = "cari";

    //    @Value("${jhipster.clientApp.name}")
    //    private String applicationName;

    //    private final CariService cariService;

    //    private final CariRepository cariRepository;

    private final CariQueryService cariQueryService;

    //    public CariResource(CariService cariService, CariRepository cariRepository, CariQueryService cariQueryService) {
    public CariResource(CariQueryService cariQueryService) {
        //        this.cariService = cariService;
        //        this.cariRepository = cariRepository;
        this.cariQueryService = cariQueryService;
    }

    /**
     * {@code GET  /caris} : get all the caris.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of caris in body.
     */
    @GetMapping("/caris")
    public ResponseEntity<List<CariDTO>> getAllCaris(
        CariCriteria criteria
        //        ,@org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Caris by criteria: {}", criteria);
        //        Page<CariDTO> page = cariQueryService.findByCriteria(criteria, pageable);
        List<CariDTO> list = cariQueryService.findByCriteria(criteria);
        //        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), list);
        //        return ResponseEntity.ok().headers(headers).body(list);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/caris/report")
    public ResponseEntity<Void> getReport(CariCriteria criteria, HttpServletResponse response) throws IOException {
        log.debug("REST request to get Caris report by criteria: {}", criteria);
        cariQueryService.generateReport(criteria, response);
        return ResponseEntity.ok().build();
    }
    //    /**
    //     * {@code POST  /caris} : Create a new cari.
    //     *
    //     * @param cariDTO the cariDTO to create.
    //     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cariDTO, or with status {@code 400 (Bad Request)} if the cari has already an ID.
    //     * @throws URISyntaxException if the Location URI syntax is incorrect.
    //     */
    //    @PostMapping("/caris")
    //    public ResponseEntity<CariDTO> createCari(@RequestBody CariDTO cariDTO) throws URISyntaxException {
    //        log.debug("REST request to save Cari : {}", cariDTO);
    //        if (cariDTO.getId() != null) {
    //            throw new BadRequestAlertException("A new cari cannot already have an ID", ENTITY_NAME, "idexists");
    //        }
    //        CariDTO result = cariService.save(cariDTO);
    //        return ResponseEntity
    //            .created(new URI("/api/caris/" + result.getId()))
    //            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
    //            .body(result);
    //    }

    //    /**
    //     * {@code PUT  /caris/:id} : Updates an existing cari.
    //     *
    //     * @param id      the id of the cariDTO to save.
    //     * @param cariDTO the cariDTO to update.
    //     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cariDTO,
    //     * or with status {@code 400 (Bad Request)} if the cariDTO is not valid,
    //     * or with status {@code 500 (Internal Server Error)} if the cariDTO couldn't be updated.
    //     * @throws URISyntaxException if the Location URI syntax is incorrect.
    //     */
    //    @PutMapping("/caris/{id}")
    //    public ResponseEntity<CariDTO> updateCari(@PathVariable(value = "id", required = false) final Long id, @RequestBody CariDTO cariDTO)
    //        throws URISyntaxException {
    //        log.debug("REST request to update Cari : {}, {}", id, cariDTO);
    //        if (cariDTO.getId() == null) {
    //            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    //        }
    //        if (!Objects.equals(id, cariDTO.getId())) {
    //            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
    //        }
    //
    //        if (!cariRepository.existsById(id)) {
    //            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    //        }
    //
    //        CariDTO result = cariService.update(cariDTO);
    //        return ResponseEntity
    //            .ok()
    //            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cariDTO.getId().toString()))
    //            .body(result);
    //    }

    //    /**
    //     * {@code PATCH  /caris/:id} : Partial updates given fields of an existing cari, field will ignore if it is null
    //     *
    //     * @param id      the id of the cariDTO to save.
    //     * @param cariDTO the cariDTO to update.
    //     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cariDTO,
    //     * or with status {@code 400 (Bad Request)} if the cariDTO is not valid,
    //     * or with status {@code 404 (Not Found)} if the cariDTO is not found,
    //     * or with status {@code 500 (Internal Server Error)} if the cariDTO couldn't be updated.
    //     * @throws URISyntaxException if the Location URI syntax is incorrect.
    //     */
    //    @PatchMapping(value = "/caris/{id}", consumes = { "application/json", "application/merge-patch+json" })
    //    public ResponseEntity<CariDTO> partialUpdateCari(
    //        @PathVariable(value = "id", required = false) final Long id,
    //        @RequestBody CariDTO cariDTO
    //    ) throws URISyntaxException {
    //        log.debug("REST request to partial update Cari partially : {}, {}", id, cariDTO);
    //        if (cariDTO.getId() == null) {
    //            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    //        }
    //        if (!Objects.equals(id, cariDTO.getId())) {
    //            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
    //        }
    //
    //        if (!cariRepository.existsById(id)) {
    //            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    //        }
    //
    //        Optional<CariDTO> result = cariService.partialUpdate(cariDTO);
    //
    //        return ResponseUtil.wrapOrNotFound(
    //            result,
    //            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cariDTO.getId().toString())
    //        );
    //    }

    //    /**
    //     * {@code GET  /caris/count} : count all the caris.
    //     *
    //     * @param criteria the criteria which the requested entities should match.
    //     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
    //     */
    //    @GetMapping("/caris/count")
    //    public ResponseEntity<Long> countCaris(CariCriteria criteria) {
    //        log.debug("REST request to count Caris by criteria: {}", criteria);
    //        return ResponseEntity.ok().body(cariQueryService.countByCriteria(criteria));
    //    }

    //    /**
    //     * {@code GET  /caris/:id} : get the "id" cari.
    //     *
    //     * @param id the id of the cariDTO to retrieve.
    //     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cariDTO, or with status {@code 404 (Not Found)}.
    //     */
    //    @GetMapping("/caris/{id}")
    //    public ResponseEntity<CariDTO> getCari(@PathVariable Long id) {
    //        log.debug("REST request to get Cari : {}", id);
    //        Optional<CariDTO> cariDTO = cariService.findOne(id);
    //        return ResponseUtil.wrapOrNotFound(cariDTO);
    //    }
    //
    //    /**
    //     * {@code DELETE  /caris/:id} : delete the "id" cari.
    //     *
    //     * @param ids the id of the cariDTO to delete.
    //     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
    //     */
    //    @DeleteMapping("/caris")
    //    public ResponseEntity<Void> deleteCaris(@RequestBody Set<Long> ids) {
    //        log.debug("REST request to delete Caris : {}", ids);
    //        ids.forEach(cariService::delete);
    //        return ResponseEntity
    //            .noContent()
    //            .headers(
    //                HeaderUtil.createEntityDeletionAlert(
    //                    applicationName,
    //                    true,
    //                    ENTITY_NAME,
    //                    ids.stream().map(String::valueOf).collect(Collectors.joining(","))
    //                )
    //            )
    //            .build();
    //    }
}
