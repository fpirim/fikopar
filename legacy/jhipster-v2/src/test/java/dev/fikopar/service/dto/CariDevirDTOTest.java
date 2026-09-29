package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CariDevirDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CariDevirDTO.class);
        CariDevirDTO cariDevirDTO1 = new CariDevirDTO();
        cariDevirDTO1.setId(1L);
        CariDevirDTO cariDevirDTO2 = new CariDevirDTO();
        assertThat(cariDevirDTO1).isNotEqualTo(cariDevirDTO2);
        cariDevirDTO2.setId(cariDevirDTO1.getId());
        assertThat(cariDevirDTO1).isEqualTo(cariDevirDTO2);
        cariDevirDTO2.setId(2L);
        assertThat(cariDevirDTO1).isNotEqualTo(cariDevirDTO2);
        cariDevirDTO1.setId(null);
        assertThat(cariDevirDTO1).isNotEqualTo(cariDevirDTO2);
    }
}
