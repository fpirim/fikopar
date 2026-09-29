package com.dizel.fikopar.service;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import tech.jhipster.service.Criteria;

public interface ReportService {
    void generateReport(Criteria criteria, HttpServletResponse response) throws IOException;
}
