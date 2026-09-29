package com.dizel.fikopar.service.mapper;

import com.dizel.fikopar.domain.CekSenetDetay;
import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.service.dto.CekSenetDetayDTO;
import com.dizel.fikopar.service.dto.MakbuzDTO;
import com.dizel.fikopar.service.dto.SicilDTO;
import java.util.List;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CekSenetDetay} and its DTO {@link CekSenetDetayDTO}.
 */
@Mapper(componentModel = "spring")
public interface CekSenetDetayMapper extends EntityMapper<CekSenetDetayDTO, CekSenetDetay> {
    @Named("toDtoCekSenetDetay")
    @Mapping(target = "sicil", source = "sicil", qualifiedByName = "sicilId")
    @Mapping(target = "makbuz", source = "makbuz", qualifiedByName = "makbuzId")
    // @Mapping(target = "cekSenet", source = "cekSenet", qualifiedByName = "cekSenetId")
    CekSenetDetayDTO toDto(CekSenetDetay s);

    @Named("sicilId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "hesapno", source = "hesapno")
    @Mapping(target = "hesapadi", source = "hesapadi")
    @Mapping(target = "sehir", source = "sehir")
    @Mapping(target = "vda", source = "vda")
    @Mapping(target = "vno", source = "vno")
    SicilDTO toDtoSicilId(Sicil sicil);

    @Named("makbuzId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "makbuzno", source = "makbuzno")
    @Mapping(target = "evraktipi", source = "evraktipi")
    @Mapping(target = "depotipi", source = "depotipi")
    @Mapping(target = "tarihi", source = "tarihi")
    @Mapping(target = "vade", source = "vade")
    @Mapping(target = "aciklama", source = "aciklama")
    @Mapping(target = "tutari", source = "tutari")
    MakbuzDTO toDtoMakbuzId(Makbuz makbuz);

    //    @Named("cekSenetId")
    //    @BeanMapping(ignoreByDefault = true)
    //    @Mapping(target = "id", source = "id")
    //    CekSenetDTO toDtoCekSenetId(CekSenet cekSenet);

    @Named("toDtoCekSenetDetays")
    @IterableMapping(qualifiedByName = "toDtoCekSenetDetay")
    List<CekSenetDetayDTO> toDtoCekSenetDetays(List<CekSenetDetay> cekSenetDetays);
}
