package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SicilMapperTest {

    private SicilMapper sicilMapper;

    @BeforeEach
    public void setUp() {
        sicilMapper = new SicilMapperImpl();
    }
}
