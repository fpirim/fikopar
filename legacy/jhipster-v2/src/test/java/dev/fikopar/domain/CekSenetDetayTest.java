package dev.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CekSenetDetayTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CekSenetDetay.class);
        CekSenetDetay cekSenetDetay1 = new CekSenetDetay();
        cekSenetDetay1.setId(1L);
        CekSenetDetay cekSenetDetay2 = new CekSenetDetay();
        cekSenetDetay2.setId(cekSenetDetay1.getId());
        assertThat(cekSenetDetay1).isEqualTo(cekSenetDetay2);
        cekSenetDetay2.setId(2L);
        assertThat(cekSenetDetay1).isNotEqualTo(cekSenetDetay2);
        cekSenetDetay1.setId(null);
        assertThat(cekSenetDetay1).isNotEqualTo(cekSenetDetay2);
    }
}
