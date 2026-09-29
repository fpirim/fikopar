package com.dizel.fikopar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.dizel.fikopar.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HareketSicilTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HareketSicil.class);
        HareketSicil hareketSicil1 = new HareketSicil();
        hareketSicil1.setId(1L);
        HareketSicil hareketSicil2 = new HareketSicil();
        hareketSicil2.setId(hareketSicil1.getId());
        assertThat(hareketSicil1).isEqualTo(hareketSicil2);
        hareketSicil2.setId(2L);
        assertThat(hareketSicil1).isNotEqualTo(hareketSicil2);
        hareketSicil1.setId(null);
        assertThat(hareketSicil1).isNotEqualTo(hareketSicil2);
    }
}
