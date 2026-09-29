package com.dizel.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CariDevirTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CariDevir.class);
        CariDevir cariDevir1 = new CariDevir();
        cariDevir1.setId(1L);
        CariDevir cariDevir2 = new CariDevir();
        cariDevir2.setId(cariDevir1.getId());
        assertThat(cariDevir1).isEqualTo(cariDevir2);
        cariDevir2.setId(2L);
        assertThat(cariDevir1).isNotEqualTo(cariDevir2);
        cariDevir1.setId(null);
        assertThat(cariDevir1).isNotEqualTo(cariDevir2);
    }
}
