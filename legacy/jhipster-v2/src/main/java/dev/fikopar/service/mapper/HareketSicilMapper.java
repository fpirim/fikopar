package dev.fikopar.service.mapper;

import dev.fikopar.domain.Cari;
import dev.fikopar.domain.HareketDetay;
import dev.fikopar.domain.HareketSicil;
import dev.fikopar.domain.Sicil;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.HareketSicilDTO;
import dev.fikopar.service.dto.SicilDTO;
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
