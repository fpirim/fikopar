package dev.fikopar.web.rest;

import dev.fikopar.service.CariToplamQueryService;
import dev.fikopar.service.criteria.CariToplamCriteria;
import dev.fikopar.service.dto.CariToplamDTO;
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
 * REST controller for managing {@link dev.fikopar.domain.Cari}.
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
