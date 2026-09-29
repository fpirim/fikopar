package dev.fikopar.service.mapper;

import dev.fikopar.domain.Katalog;
import dev.fikopar.service.dto.KatalogDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Katalog} and its DTO {@link KatalogDTO}.
 */
@Mapper(componentModel = "spring")
public interface KatalogMapper extends EntityMapper<KatalogDTO, Katalog> {}
