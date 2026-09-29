package com.dizel.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MakbuzMapperTest {

    private MakbuzMapper makbuzMapper;

    @BeforeEach
    public void setUp() {
        makbuzMapper = new MakbuzMapperImpl();
    }
}
