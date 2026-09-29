package dev.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HareketDetayTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HareketDetay.class);
        HareketDetay hareketDetay1 = new HareketDetay();
        hareketDetay1.setId(1L);
        HareketDetay hareketDetay2 = new HareketDetay();
        hareketDetay2.setId(hareketDetay1.getId());
        assertThat(hareketDetay1).isEqualTo(hareketDetay2);
        hareketDetay2.setId(2L);
        assertThat(hareketDetay1).isNotEqualTo(hareketDetay2);
        hareketDetay1.setId(null);
        assertThat(hareketDetay1).isNotEqualTo(hareketDetay2);
    }
}
