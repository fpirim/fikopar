package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Katalog;
import com.dizel.fikopar.service.dto.KatalogDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Katalog} and its DTO {@link KatalogDTO}.
 */
@Mapper(componentModel = "spring")
public interface KatalogMapper extends EntityMapper<KatalogDTO, Katalog> {}
