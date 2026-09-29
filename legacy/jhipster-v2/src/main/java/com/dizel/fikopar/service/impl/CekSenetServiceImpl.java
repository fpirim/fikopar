package com.dizel.fikopar.service.impl;

import com.dizel.fikopar.domain.CekSenet;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.domain.enumeration.IslemTipi;
import com.dizel.fikopar.repository.CekSenetDetayRepository;
import com.dizel.fikopar.repository.CekSenetRepository;
import com.dizel.fikopar.service.CekSenetService;
import com.dizel.fikopar.service.MakbuzService;
import com.dizel.fikopar.service.dto.CekSenetDTO;
import com.dizel.fikopar.service.dto.CekSenetDetayDTO;
import com.dizel.fikopar.service.dto.MakbuzDTO;
import com.dizel.fikopar.service.mapper.CekSenetDetayMapper;
import com.dizel.fikopar.service.mapper.CekSenetMapper;
import com.dizel.fikopar.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CekSenet}.
 */
@Service
@Transactional
public class CekSenetServiceImpl implements CekSenetService {

    private final Logger log = LoggerFactory.getLogger(CekSenetServiceImpl.class);

    private final CekSenetRepository cekSenetRepository;
    private final CekSenetDetayRepository cekSenetDetayRepository;
    private final CekSenetMapper cekSenetMapper;
    private final CekSenetDetayMapper cekSenetDetayMapper;
    private final MakbuzService makbuzService;

    public CekSenetServiceImpl(
        CekSenetRepository cekSenetRepository,
        CekSenetDetayRepository cekSenetDetayRepository,
        CekSenetMapper cekSenetMapper,
        CekSenetDetayMapper cekSenetDetayMapper,
        MakbuzService makbuzService
    ) {
        this.cekSenetRepository = cekSenetRepository;
        this.cekSenetDetayRepository = cekSenetDetayRepository;
        this.cekSenetMapper = cekSenetMapper;
        this.cekSenetDetayMapper = cekSenetDetayMapper;
        this.makbuzService = makbuzService;
    }

    private void createOrUpdateMakbuz(CekSenetDTO cekSenetDTO) {
        LocalDate tarihi = cekSenetDTO.getTarihi();
        DepoTipi depoTipi = cekSenetDTO.getDepotipi();
        EvrakTipi makbuzTipi = null;
        String aciklama = "";

        switch (cekSenetDTO.getEvraktipi()) {
            case ALACAK_CEKI:
                aciklama = "ALACAK ÇEKİ İŞLEMİ";
                makbuzTipi = EvrakTipi.TAHSILAT_MAKBUZU;
                break;
            case ALACAK_SENETI:
                aciklama = "ALACAK SENETİ İŞLEMİ";
                makbuzTipi = EvrakTipi.TAHSILAT_MAKBUZU;
                break;
            case BORC_CEKI:
                aciklama = "BORÇ ÇEKİ İŞLEMİ";
                makbuzTipi = EvrakTipi.TEDIYE_MAKBUZU;
                break;
            case BORC_SENETI:
                aciklama = "BORÇ SENETİ İŞLEMİ";
                makbuzTipi = EvrakTipi.TEDIYE_MAKBUZU;
                break;
        }

        String makbuzno = makbuzService.generateMakbuzNo(makbuzTipi, depoTipi, tarihi);

        CekSenetDetayDTO cekSenetDetayDTO;
        if (cekSenetDTO.getId() == null) {
            cekSenetDetayDTO = new CekSenetDetayDTO();
            cekSenetDetayDTO.setMakbuz(new MakbuzDTO());
        } else {
            List<CekSenetDetayDTO> cekSenetDetays = cekSenetDetayMapper.toDtoCekSenetDetays(
                cekSenetDetayRepository.findAllByCekSenetId(cekSenetDTO.getId())
            );
            if (cekSenetDetays.size() > 1) {
                throw new BadRequestAlertException("Invalid update", "cekSenet", "cekSenet.errors.invalidUpdate");
            }
            cekSenetDetayDTO = cekSenetDetays.get(0);
            MakbuzDTO makbuzDTO = cekSenetDetayDTO.getMakbuz();

            if (
                makbuzDTO.getEvraktipi().equals(makbuzTipi) &&
                makbuzDTO.getTarihi().getYear() == tarihi.getYear() &&
                makbuzDTO.getDepotipi().equals(depoTipi)
            ) {
                makbuzno = makbuzDTO.getMakbuzno();
            }
        }

        cekSenetDTO.setCekSenetDetays(List.of(cekSenetDetayDTO));

        cekSenetDetayDTO.setTarihi(cekSenetDTO.getTarihi());
        cekSenetDetayDTO.setTutar(cekSenetDTO.getTutar());
        cekSenetDetayDTO.setSicil(cekSenetDTO.getSicil());
        cekSenetDetayDTO.setIslemtipi(IslemTipi.PORTFOY);

        MakbuzDTO makbuzDTO = cekSenetDetayDTO.getMakbuz();
        makbuzDTO.setSicil(cekSenetDetayDTO.getSicil());
        makbuzDTO.setMakbuzno(makbuzno);
        makbuzDTO.setAciklama(aciklama);
        makbuzDTO.setEvraktipi(makbuzTipi);
        makbuzDTO.setDepotipi(depoTipi);
        makbuzDTO.setTarihi(cekSenetDetayDTO.getTarihi());
        makbuzDTO.setVade(cekSenetDTO.getVade());
        makbuzDTO.setTutari(cekSenetDetayDTO.getTutar());

        makbuzService.createOrUpdateCari(makbuzDTO);
    }

    @Override
    public CekSenetDTO save(CekSenetDTO cekSenetDTO) {
        log.debug("Request to save CekSenet : {}", cekSenetDTO);

        createOrUpdateMakbuz(cekSenetDTO);
        CekSenet cekSenet = cekSenetMapper.toEntity(cekSenetDTO);
        cekSenet = cekSenetRepository.save(cekSenet);
        return cekSenetMapper.toDto(cekSenet);
    }

    @Override
    public CekSenetDTO update(CekSenetDTO cekSenetDTO) {
        log.debug("Request to update CekSenet : {}", cekSenetDTO);

        createOrUpdateMakbuz(cekSenetDTO);
        CekSenet cekSenet = cekSenetMapper.toEntity(cekSenetDTO);
        cekSenet = cekSenetRepository.save(cekSenet);
        return cekSenetMapper.toDto(cekSenet);
    }

    @Override
    public Optional<CekSenetDTO> partialUpdate(CekSenetDTO cekSenetDTO) {
        log.debug("Request to partially update CekSenet : {}", cekSenetDTO);

        createOrUpdateMakbuz(cekSenetDTO);
        return cekSenetRepository
            .findById(cekSenetDTO.getId())
            .map(existingCekSenet -> {
                cekSenetMapper.partialUpdate(existingCekSenet, cekSenetDTO);

                return existingCekSenet;
            })
            .map(cekSenetRepository::save)
            .map(cekSenetMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CekSenetDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CekSenets");
        return cekSenetRepository.findAll(pageable).map(cekSenetMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CekSenetDTO> findOne(Long id) {
        log.debug("Request to get CekSenet : {}", id);
        return cekSenetRepository.findById(id).map(cekSenetMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CekSenet : {}", id);
        cekSenetRepository.deleteById(id);
    }

    public String generateCekSenetNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi) {
        if (evrakTipi == null || depoTipi == null || tarihi == null) return "";

        String prefix = "";
        switch (evrakTipi) {
            case ALACAK_CEKI:
                prefix = "AC";
                break;
            case ALACAK_SENETI:
                prefix = "AS";
                break;
            case BORC_CEKI:
                prefix = "BC";
                break;
            case BORC_SENETI:
                prefix = "BS";
                break;
        }

        return cekSenetRepository
            .findMaxCekSenetNo(evrakTipi, depoTipi, LocalDate.of(tarihi.getYear(), 1, 1), LocalDate.of(tarihi.getYear(), 12, 31))
            .filter(c -> c.matches("(AC|AS|BC|BS)\\d{4}\\d{1}\\d+"))
            .map(c -> c.substring(0, 8) + String.format("%06d", Integer.parseInt(c.substring(8)) + 1))
            .orElse(String.format("%s%04d%s%06d", prefix, tarihi.getYear(), depoTipi == DepoTipi.DEPO1 ? "0" : "9", 1));
    }
}
