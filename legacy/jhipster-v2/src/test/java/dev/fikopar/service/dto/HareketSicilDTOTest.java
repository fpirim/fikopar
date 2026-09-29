package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HareketSicilDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(HareketSicilDTO.class);
        HareketSicilDTO hareketSicilDTO1 = new HareketSicilDTO();
        hareketSicilDTO1.setId(1L);
        HareketSicilDTO hareketSicilDTO2 = new HareketSicilDTO();
        assertThat(hareketSicilDTO1).isNotEqualTo(hareketSicilDTO2);
        hareketSicilDTO2.setId(hareketSicilDTO1.getId());
        assertThat(hareketSicilDTO1).isEqualTo(hareketSicilDTO2);
        hareketSicilDTO2.setId(2L);
        assertThat(hareketSicilDTO1).isNotEqualTo(hareketSicilDTO2);
        hareketSicilDTO1.setId(null);
        assertThat(hareketSicilDTO1).isNotEqualTo(hareketSicilDTO2);
    }
}
