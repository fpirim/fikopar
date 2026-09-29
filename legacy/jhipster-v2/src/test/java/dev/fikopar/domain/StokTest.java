package dev.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StokTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Stok.class);
        Stok stok1 = new Stok();
        stok1.setId(1L);
        Stok stok2 = new Stok();
        stok2.setId(stok1.getId());
        assertThat(stok1).isEqualTo(stok2);
        stok2.setId(2L);
        assertThat(stok1).isNotEqualTo(stok2);
        stok1.setId(null);
        assertThat(stok1).isNotEqualTo(stok2);
    }
}
