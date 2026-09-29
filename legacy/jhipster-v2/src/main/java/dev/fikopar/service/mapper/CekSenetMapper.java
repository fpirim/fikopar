package dev.fikopar.service.mapper;

import dev.fikopar.domain.CekSenet;
import dev.fikopar.domain.CekSenetDetay;
import dev.fikopar.domain.Sicil;
import dev.fikopar.service.dto.CekSenetDTO;
import dev.fikopar.service.dto.SicilDTO;
import java.util.List;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CekSenet} and its DTO {@link CekSenetDTO}.
 */
@Mapper(componentModel = "spring", uses = CekSenetDetayMapper.class)
public interface CekSenetMapper extends EntityMapper<CekSenetDTO, CekSenet>, LazyLoadingAwareMapper {
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    @Mapping(target = "cekSenetDetays", source = "cekSenetDetays", qualifiedByName = "toDtoCekSenetDetays")
    CekSenetDTO toDto(CekSenet s);

    @Condition
    default boolean isNotLazyLoadedCekSenetDetays(List<CekSenetDetay> cekSenetDetays) {
        return isNotLazyLoaded(cekSenetDetays);
    }
}
