package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HareketDetayDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(HareketDetayDTO.class);
        HareketDetayDTO hareketDetayDTO1 = new HareketDetayDTO();
        hareketDetayDTO1.setId(1L);
        HareketDetayDTO hareketDetayDTO2 = new HareketDetayDTO();
        assertThat(hareketDetayDTO1).isNotEqualTo(hareketDetayDTO2);
        hareketDetayDTO2.setId(hareketDetayDTO1.getId());
        assertThat(hareketDetayDTO1).isEqualTo(hareketDetayDTO2);
        hareketDetayDTO2.setId(2L);
        assertThat(hareketDetayDTO1).isNotEqualTo(hareketDetayDTO2);
        hareketDetayDTO1.setId(null);
        assertThat(hareketDetayDTO1).isNotEqualTo(hareketDetayDTO2);
    }
}
