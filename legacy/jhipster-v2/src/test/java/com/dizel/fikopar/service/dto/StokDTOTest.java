package com.dizel.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StokDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(StokDTO.class);
        StokDTO stokDTO1 = new StokDTO();
        stokDTO1.setId(1L);
        StokDTO stokDTO2 = new StokDTO();
        assertThat(stokDTO1).isNotEqualTo(stokDTO2);
        stokDTO2.setId(stokDTO1.getId());
        assertThat(stokDTO1).isEqualTo(stokDTO2);
        stokDTO2.setId(2L);
        assertThat(stokDTO1).isNotEqualTo(stokDTO2);
        stokDTO1.setId(null);
        assertThat(stokDTO1).isNotEqualTo(stokDTO2);
    }
}
