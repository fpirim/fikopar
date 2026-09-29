package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.repository.custom.CariToplam;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.CariToplamDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Cari} and its DTO {@link CariDTO}.
 */
@Mapper(componentModel = "spring")
public interface CariMapper extends EntityMapper<CariDTO, Cari> {
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    CariDTO toDto(Cari s);

    @Named("sicilId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    SicilDTO toDtoSicilId(Sicil sicil);

    CariToplamDTO toCariToplamDto(CariToplam s);
    List<CariToplamDTO> toCariToplamDto(List<CariToplam> list);
}
