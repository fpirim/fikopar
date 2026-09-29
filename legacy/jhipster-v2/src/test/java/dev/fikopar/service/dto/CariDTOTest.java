package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CariDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CariDTO.class);
        CariDTO cariDTO1 = new CariDTO();
        cariDTO1.setId(1L);
        CariDTO cariDTO2 = new CariDTO();
        assertThat(cariDTO1).isNotEqualTo(cariDTO2);
        cariDTO2.setId(cariDTO1.getId());
        assertThat(cariDTO1).isEqualTo(cariDTO2);
        cariDTO2.setId(2L);
        assertThat(cariDTO1).isNotEqualTo(cariDTO2);
        cariDTO1.setId(null);
        assertThat(cariDTO1).isNotEqualTo(cariDTO2);
    }
}
