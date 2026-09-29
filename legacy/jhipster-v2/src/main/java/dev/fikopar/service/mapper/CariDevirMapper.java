package dev.fikopar.service.mapper;

import dev.fikopar.domain.Cari;
import dev.fikopar.domain.CariDevir;
import dev.fikopar.domain.Sicil;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.CariDevirDTO;
import dev.fikopar.service.dto.SicilDTO;
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
