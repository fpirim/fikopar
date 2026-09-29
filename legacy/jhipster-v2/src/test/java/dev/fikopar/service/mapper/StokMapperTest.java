package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StokMapperTest {

    private StokMapper stokMapper;

    @BeforeEach
    public void setUp() {
        stokMapper = new StokMapperImpl();
    }
}
