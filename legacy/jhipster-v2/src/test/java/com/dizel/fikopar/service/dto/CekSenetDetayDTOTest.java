package com.dizel.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CekSenetDetayDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CekSenetDetayDTO.class);
        CekSenetDetayDTO cekSenetDetayDTO1 = new CekSenetDetayDTO();
        cekSenetDetayDTO1.setId(1L);
        CekSenetDetayDTO cekSenetDetayDTO2 = new CekSenetDetayDTO();
        assertThat(cekSenetDetayDTO1).isNotEqualTo(cekSenetDetayDTO2);
        cekSenetDetayDTO2.setId(cekSenetDetayDTO1.getId());
        assertThat(cekSenetDetayDTO1).isEqualTo(cekSenetDetayDTO2);
        cekSenetDetayDTO2.setId(2L);
        assertThat(cekSenetDetayDTO1).isNotEqualTo(cekSenetDetayDTO2);
        cekSenetDetayDTO1.setId(null);
        assertThat(cekSenetDetayDTO1).isNotEqualTo(cekSenetDetayDTO2);
    }
}
