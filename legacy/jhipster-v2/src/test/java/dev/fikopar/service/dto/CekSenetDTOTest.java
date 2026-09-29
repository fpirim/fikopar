package dev.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CekSenetDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CekSenetDTO.class);
        CekSenetDTO cekSenetDTO1 = new CekSenetDTO();
        cekSenetDTO1.setId(1L);
        CekSenetDTO cekSenetDTO2 = new CekSenetDTO();
        assertThat(cekSenetDTO1).isNotEqualTo(cekSenetDTO2);
        cekSenetDTO2.setId(cekSenetDTO1.getId());
        assertThat(cekSenetDTO1).isEqualTo(cekSenetDTO2);
        cekSenetDTO2.setId(2L);
        assertThat(cekSenetDTO1).isNotEqualTo(cekSenetDTO2);
        cekSenetDTO1.setId(null);
        assertThat(cekSenetDTO1).isNotEqualTo(cekSenetDTO2);
    }
}
