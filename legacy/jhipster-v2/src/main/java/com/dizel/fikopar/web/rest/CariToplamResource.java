package com.dizel.fikopar.web.rest;

import com.dizel.fikopar.service.CariToplamQueryService;
import com.dizel.fikopar.service.criteria.CariToplamCriteria;
import com.dizel.fikopar.service.dto.CariToplamDTO;
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
public class CariToplamResource {

    private final Logger log = LoggerFactory.getLogger(CariToplamResource.class);

    private final CariToplamQueryService cariToplamQueryService;

    public CariToplamResource(CariToplamQueryService cariToplamQueryService) {
        this.cariToplamQueryService = cariToplamQueryService;
    }

    /**
     * {@code GET  /caritoplams} : get all the caris.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of caris in body.
     */
    @GetMapping("/caritoplams")
    public ResponseEntity<List<CariToplamDTO>> getAllCariToplams(CariToplamCriteria criteria) {
        log.debug("REST request to get CariToplams by criteria: {}", criteria);
        List<CariToplamDTO> list = cariToplamQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/caritoplams/report")
    public ResponseEntity<Void> getReport(CariToplamCriteria criteria, HttpServletResponse response) throws IOException {
        log.debug("REST request to get CariToplams report by criteria: {}", criteria);
        cariToplamQueryService.generateReport(criteria, response);
        return ResponseEntity.ok().build();
    }
}
