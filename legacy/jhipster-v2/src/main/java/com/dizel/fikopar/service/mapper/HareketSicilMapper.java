package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.HareketDetay;
import com.dizel.fikopar.domain.HareketSicil;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.HareketSicilDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import java.util.List;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HareketSicil} and its DTO {@link HareketSicilDTO}.
 */
@Mapper(componentModel = "spring", uses = HareketDetayMapper.class)
public interface HareketSicilMapper extends EntityMapper<HareketSicilDTO, HareketSicil>, LazyLoadingAwareMapper {
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    @Mapping(target = "cari", source = "cari", qualifiedByName = "cariId")
    @Mapping(target = "hareketDetays", qualifiedByName = "toDtoHareketDetays")
    HareketSicilDTO toDto(HareketSicil s);

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

    @Condition
    default boolean isNotLazyLoadedHareketDetays(List<HareketDetay> hareketDetays) {
        return isNotLazyLoaded(hareketDetays);
    }
}
