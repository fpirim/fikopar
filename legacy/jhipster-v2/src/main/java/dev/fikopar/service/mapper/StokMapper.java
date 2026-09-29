package dev.fikopar.service.mapper;

import dev.fikopar.domain.Katalog;
import dev.fikopar.domain.Stok;
import dev.fikopar.service.dto.KatalogDTO;
import dev.fikopar.service.dto.StokDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Stok} and its DTO {@link StokDTO}.
 */
@Mapper(componentModel = "spring")
public interface StokMapper extends EntityMapper<StokDTO, Stok> {
    @Mapping(target = "katalog", source = "katalog", qualifiedByName = "katalogId")
    StokDTO toDto(Stok s);

    @Named("katalogId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "katalogadi", source = "katalogadi")
    KatalogDTO toDtoKatalogId(Katalog katalog);
}
