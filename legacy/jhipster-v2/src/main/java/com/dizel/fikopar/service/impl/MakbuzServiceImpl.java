package com.dizel.fikopar.service.impl;

import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.MakbuzRepository;
import com.dizel.fikopar.service.MakbuzService;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.dto.MakbuzDTO;
import com.dizel.fikopar.service.mapper.CariMapper;
import com.dizel.fikopar.service.mapper.MakbuzMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Makbuz}.
 */
@Service
@Transactional
public class MakbuzServiceImpl implements MakbuzService {

    private final Logger log = LoggerFactory.getLogger(MakbuzServiceImpl.class);

    private final MakbuzRepository makbuzRepository;

    private final MakbuzMapper makbuzMapper;

    private final CariMapper cariMapper;

    public MakbuzServiceImpl(MakbuzRepository makbuzRepository, MakbuzMapper makbuzMapper, CariMapper cariMapper) {
        this.makbuzRepository = makbuzRepository;
        this.makbuzMapper = makbuzMapper;
        this.cariMapper = cariMapper;
    }

    public void createOrUpdateCari(MakbuzDTO makbuzDTO) {
        CariDTO cariDTO;
        if (makbuzDTO.getId() == null) {
            cariDTO = new CariDTO();
        } else {
            cariDTO = makbuzRepository.findById(makbuzDTO.getId()).map(m -> cariMapper.toDto(m.getCari())).orElse(new CariDTO());
        }

        cariDTO.setSicil(makbuzDTO.getSicil());
        cariDTO.setDepotipi(makbuzDTO.getDepotipi());
        cariDTO.setEvrakno(makbuzDTO.getMakbuzno());
        cariDTO.setTipi(makbuzDTO.getEvraktipi());
        cariDTO.setTarih(makbuzDTO.getTarihi());
        cariDTO.setVade(makbuzDTO.getVade());
        cariDTO.setAciklama(makbuzDTO.getAciklama());
        cariDTO.setBorc(BigDecimal.ZERO);
        cariDTO.setAlacak(BigDecimal.ZERO);
        if (makbuzDTO.getEvraktipi() == EvrakTipi.TAHSILAT_MAKBUZU) {
            cariDTO.setAlacak(makbuzDTO.getTutari());
        } else {
            cariDTO.setBorc(makbuzDTO.getTutari());
        }
        makbuzDTO.setCari(cariDTO);
    }

    @Override
    public MakbuzDTO save(MakbuzDTO makbuzDTO) {
        log.debug("Request to save Makbuz : {}", makbuzDTO);

        createOrUpdateCari(makbuzDTO);
        Makbuz makbuz = makbuzMapper.toEntity(makbuzDTO);
        makbuz = makbuzRepository.save(makbuz);
        return makbuzMapper.toDto(makbuz);
    }

    @Override
    public MakbuzDTO update(MakbuzDTO makbuzDTO) {
        log.debug("Request to update Makbuz : {}", makbuzDTO);

        createOrUpdateCari(makbuzDTO);
        Makbuz makbuz = makbuzMapper.toEntity(makbuzDTO);
        makbuz = makbuzRepository.save(makbuz);
        return makbuzMapper.toDto(makbuz);
    }

    @Override
    public Optional<MakbuzDTO> partialUpdate(MakbuzDTO makbuzDTO) {
        log.debug("Request to partially update Makbuz : {}", makbuzDTO);

        createOrUpdateCari(makbuzDTO);
        return makbuzRepository
            .findById(makbuzDTO.getId())
            .map(existingMakbuz -> {
                makbuzMapper.partialUpdate(existingMakbuz, makbuzDTO);

                return existingMakbuz;
            })
            .map(makbuzRepository::save)
            .map(makbuzMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MakbuzDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Makbuzs");
        return makbuzRepository.findAll(pageable).map(makbuzMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MakbuzDTO> findOne(Long id) {
        log.debug("Request to get Makbuz : {}", id);
        return makbuzRepository.findById(id).map(makbuzMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Makbuz : {}", id);
        makbuzRepository.deleteById(id);
    }

    public String generateMakbuzNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi) {
        if (evrakTipi == null || depoTipi == null || tarihi == null) return "";
        return makbuzRepository
            .findMaxMakbuzNo(evrakTipi, depoTipi, LocalDate.of(tarihi.getYear(), 1, 1), LocalDate.of(tarihi.getYear(), 12, 31))
            .filter(m -> m.matches("(TAH|TED)\\d{4}\\d{1}\\d+"))
            .map(m -> m.substring(0, 8) + String.format("%06d", Integer.parseInt(m.substring(8)) + 1))
            .orElse(
                String.format(
                    "%s%04d%s%06d",
                    evrakTipi == EvrakTipi.TAHSILAT_MAKBUZU ? "TAH" : "TED",
                    tarihi.getYear(),
                    depoTipi == DepoTipi.DEPO1 ? "0" : "9",
                    1
                )
            );
    }
}
