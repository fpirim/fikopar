package com.dizel.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CekSenetTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CekSenet.class);
        CekSenet cekSenet1 = new CekSenet();
        cekSenet1.setId(1L);
        CekSenet cekSenet2 = new CekSenet();
        cekSenet2.setId(cekSenet1.getId());
        assertThat(cekSenet1).isEqualTo(cekSenet2);
        cekSenet2.setId(2L);
        assertThat(cekSenet1).isNotEqualTo(cekSenet2);
        cekSenet1.setId(null);
        assertThat(cekSenet1).isNotEqualTo(cekSenet2);
    }
}
