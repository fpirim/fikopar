package dev.fikopar.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HareketSicilMapperTest {

    private HareketSicilMapper hareketSicilMapper;

    @BeforeEach
    public void setUp() {
        hareketSicilMapper = new HareketSicilMapperImpl();
    }
}
