package com.dizel.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class KatalogDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(KatalogDTO.class);
        KatalogDTO katalogDTO1 = new KatalogDTO();
        katalogDTO1.setId(1L);
        KatalogDTO katalogDTO2 = new KatalogDTO();
        assertThat(katalogDTO1).isNotEqualTo(katalogDTO2);
        katalogDTO2.setId(katalogDTO1.getId());
        assertThat(katalogDTO1).isEqualTo(katalogDTO2);
        katalogDTO2.setId(2L);
        assertThat(katalogDTO1).isNotEqualTo(katalogDTO2);
        katalogDTO1.setId(null);
        assertThat(katalogDTO1).isNotEqualTo(katalogDTO2);
    }
}
