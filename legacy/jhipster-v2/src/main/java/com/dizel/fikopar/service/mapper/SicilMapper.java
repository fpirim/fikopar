package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.service.dto.SicilDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Sicil} and its DTO {@link SicilDTO}.
 */
@Mapper(componentModel = "spring")
public interface SicilMapper extends EntityMapper<SicilDTO, Sicil> {}
