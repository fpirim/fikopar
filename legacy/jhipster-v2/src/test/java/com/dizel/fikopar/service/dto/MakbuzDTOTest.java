package com.dizel.fikopar.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MakbuzDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MakbuzDTO.class);
        MakbuzDTO makbuzDTO1 = new MakbuzDTO();
        makbuzDTO1.setId(1L);
        MakbuzDTO makbuzDTO2 = new MakbuzDTO();
        assertThat(makbuzDTO1).isNotEqualTo(makbuzDTO2);
        makbuzDTO2.setId(makbuzDTO1.getId());
        assertThat(makbuzDTO1).isEqualTo(makbuzDTO2);
        makbuzDTO2.setId(2L);
        assertThat(makbuzDTO1).isNotEqualTo(makbuzDTO2);
        makbuzDTO1.setId(null);
        assertThat(makbuzDTO1).isNotEqualTo(makbuzDTO2);
    }
}
