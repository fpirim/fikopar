package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SicilDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SicilDTO.class);
        SicilDTO sicilDTO1 = new SicilDTO();
        sicilDTO1.setId(1L);
        SicilDTO sicilDTO2 = new SicilDTO();
        assertThat(sicilDTO1).isNotEqualTo(sicilDTO2);
        sicilDTO2.setId(sicilDTO1.getId());
        assertThat(sicilDTO1).isEqualTo(sicilDTO2);
        sicilDTO2.setId(2L);
        assertThat(sicilDTO1).isNotEqualTo(sicilDTO2);
        sicilDTO1.setId(null);
        assertThat(sicilDTO1).isNotEqualTo(sicilDTO2);
    }
}
