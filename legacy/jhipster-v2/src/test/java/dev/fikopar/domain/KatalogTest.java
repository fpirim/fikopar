package dev.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class KatalogTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Katalog.class);
        Katalog katalog1 = new Katalog();
        katalog1.setId(1L);
        Katalog katalog2 = new Katalog();
        katalog2.setId(katalog1.getId());
        assertThat(katalog1).isEqualTo(katalog2);
        katalog2.setId(2L);
        assertThat(katalog1).isNotEqualTo(katalog2);
        katalog1.setId(null);
        assertThat(katalog1).isNotEqualTo(katalog2);
    }
}
