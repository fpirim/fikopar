package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CariMapperTest {

    private CariMapper cariMapper;

    @BeforeEach
    public void setUp() {
        cariMapper = new CariMapperImpl();
    }
}
