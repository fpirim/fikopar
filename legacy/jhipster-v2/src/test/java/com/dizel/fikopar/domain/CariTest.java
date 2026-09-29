package com.dizel.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CariTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cari.class);
        Cari cari1 = new Cari();
        cari1.setId(1L);
        Cari cari2 = new Cari();
        cari2.setId(cari1.getId());
        assertThat(cari1).isEqualTo(cari2);
        cari2.setId(2L);
        assertThat(cari1).isNotEqualTo(cari2);
        cari1.setId(null);
        assertThat(cari1).isNotEqualTo(cari2);
    }
}
