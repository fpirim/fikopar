package com.dizel.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HareketDetayMapperTest {

    private HareketDetayMapper hareketDetayMapper;

    @BeforeEach
    public void setUp() {
        hareketDetayMapper = new HareketDetayMapperImpl();
    }
}
