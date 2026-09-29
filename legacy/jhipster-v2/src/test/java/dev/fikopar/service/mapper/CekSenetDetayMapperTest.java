package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CekSenetDetayMapperTest {

    private CekSenetDetayMapper cekSenetDetayMapper;

    @BeforeEach
    public void setUp() {
        cekSenetDetayMapper = new CekSenetDetayMapperImpl();
    }
}
