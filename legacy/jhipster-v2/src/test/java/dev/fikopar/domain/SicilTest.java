package dev.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SicilTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sicil.class);
        Sicil sicil1 = new Sicil();
        sicil1.setId(1L);
        Sicil sicil2 = new Sicil();
        sicil2.setId(sicil1.getId());
        assertThat(sicil1).isEqualTo(sicil2);
        sicil2.setId(2L);
        assertThat(sicil1).isNotEqualTo(sicil2);
        sicil1.setId(null);
        assertThat(sicil1).isNotEqualTo(sicil2);
    }
}
