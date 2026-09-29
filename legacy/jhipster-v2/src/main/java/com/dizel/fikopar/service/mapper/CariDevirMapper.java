package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.CariDevir;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.CariDevirDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link CariDevir} and its DTO {@link CariDevirDTO}.
 */
@Mapper(componentModel = "spring")
public interface CariDevirMapper extends EntityMapper<CariDevirDTO, CariDevir> {
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    @Mapping(target = "cari", source = "cari", qualifiedByName = "cariId")
    CariDevirDTO toDto(CariDevir s);

    @Named("sicilId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "hesapno", source = "hesapno")
    @Mapping(target = "hesapadi", source = "hesapadi")
    @Mapping(target = "sehir", source = "sehir")
    @Mapping(target = "vda", source = "vda")
    @Mapping(target = "vno", source = "vno")
    SicilDTO toDtoSicilId(Sicil sicil);

    @Named("cariId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CariDTO toDtoCariId(Cari cari);
}
