package dev.fikopar.service.mapper;

import dev.fikopar.domain.HareketDetay;
import dev.fikopar.service.dto.HareketDetayDTO;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link HareketDetay} and its DTO {@link HareketDetayDTO}.
 */
@Mapper(componentModel = "spring")
public interface HareketDetayMapper extends EntityMapper<HareketDetayDTO, HareketDetay> {
    // @Mapping(target = "hareketSicil", source = "hareketSicil", qualifiedByName = "hareketSicilId")
    @Named("toDtoHareketDetay")
    HareketDetayDTO toDto(HareketDetay s);

    //    @Named("hareketSicilId")
    //    @BeanMapping(ignoreByDefault = true)
    //    @Mapping(target = "id", source = "id")
    //    HareketSicilDTO toDtoHareketSicilId(HareketSicil hareketSicil);

    @IterableMapping(qualifiedByName = "toDtoHareketDetay")
    @Named("toDtoHareketDetays")
    List<HareketDetayDTO> toDtoHareketDetays(List<HareketDetay> hareketDetays);
}
