package dev.fikopar.service.mapper;

import dev.fikopar.domain.Cari;
import dev.fikopar.domain.Makbuz;
import dev.fikopar.domain.Sicil;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.MakbuzDTO;
import dev.fikopar.service.dto.SicilDTO;
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
