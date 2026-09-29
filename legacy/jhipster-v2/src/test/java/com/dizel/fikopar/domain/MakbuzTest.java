package com.dizel.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MakbuzTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Makbuz.class);
        Makbuz makbuz1 = new Makbuz();
        makbuz1.setId(1L);
        Makbuz makbuz2 = new Makbuz();
        makbuz2.setId(makbuz1.getId());
        assertThat(makbuz1).isEqualTo(makbuz2);
        makbuz2.setId(2L);
        assertThat(makbuz1).isNotEqualTo(makbuz2);
        makbuz1.setId(null);
        assertThat(makbuz1).isNotEqualTo(makbuz2);
    }
}
