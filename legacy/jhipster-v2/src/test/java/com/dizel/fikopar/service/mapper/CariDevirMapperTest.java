package com.dizel.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CariDevirMapperTest {

    private CariDevirMapper cariDevirMapper;

    @BeforeEach
    public void setUp() {
        cariDevirMapper = new CariDevirMapperImpl();
    }
}
