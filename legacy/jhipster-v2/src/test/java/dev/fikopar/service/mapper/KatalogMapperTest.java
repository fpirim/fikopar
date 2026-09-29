package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KatalogMapperTest {

    private KatalogMapper katalogMapper;

    @BeforeEach
    public void setUp() {
        katalogMapper = new KatalogMapperImpl();
    }
}
