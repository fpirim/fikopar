package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.MakbuzDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Makbuz} and its DTO {@link MakbuzDTO}.
 */
@Mapper(componentModel = "spring")
public interface MakbuzMapper extends EntityMapper<MakbuzDTO, Makbuz> {
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    @Mapping(target = "cari", source = "cari", qualifiedByName = "cariId")
    MakbuzDTO toDto(Makbuz s);

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
