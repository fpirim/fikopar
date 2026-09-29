package dev.fikopar.service.mapper;

import dev.fikopar.domain.Sicil;
import dev.fikopar.service.dto.SicilDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Sicil} and its DTO {@link SicilDTO}.
 */
@Mapper(componentModel = "spring")
public interface SicilMapper extends EntityMapper<SicilDTO, Sicil> {}
