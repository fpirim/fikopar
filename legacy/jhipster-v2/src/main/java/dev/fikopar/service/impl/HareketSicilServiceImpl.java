package dev.fikopar.service.impl;

import dev.fikopar.domain.HareketSicil;
import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.repository.HareketSicilRepository;
import dev.fikopar.service.HareketSicilService;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.HareketSicilDTO;
import dev.fikopar.service.mapper.CariMapper;
import dev.fikopar.service.mapper.HareketSicilMapper;
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
 * Service Implementation for managing {@link HareketSicil}.
 */
@Service
@Transactional
public class HareketSicilServiceImpl implements HareketSicilService {

    private final Logger log = LoggerFactory.getLogger(HareketSicilServiceImpl.class);

    private final HareketSicilRepository hareketSicilRepository;

    private final HareketSicilMapper hareketSicilMapper;
    private final CariMapper cariMapper;

    public HareketSicilServiceImpl(
        HareketSicilRepository hareketSicilRepository,
        HareketSicilMapper hareketSicilMapper,
        CariMapper cariMapper
    ) {
        this.hareketSicilRepository = hareketSicilRepository;
        this.hareketSicilMapper = hareketSicilMapper;
        this.cariMapper = cariMapper;
    }

    private void createOrUpdateCari(HareketSicilDTO hareketSicilDTO) {
        CariDTO cariDTO;
        if (hareketSicilDTO.getId() == null) {
            cariDTO = new CariDTO();
        } else {
            cariDTO =
                hareketSicilRepository.findById(hareketSicilDTO.getId()).map(h -> cariMapper.toDto(h.getCari())).orElse(new CariDTO());
        }

        String aciklama = "";
        switch (hareketSicilDTO.getEvraktipi()) {
            case IRSALIYELI_SATIS_FATURASI:
            case SATIS_FATURASI:
            case SATIS_IRSALIYESI:
                aciklama = "STOK SATIŞ İŞLEMİ";
                break;
            default:
                aciklama = "STOK ALIŞ İŞLEMİ";
                break;
        }

        cariDTO.setSicil(hareketSicilDTO.getSicil());
        cariDTO.setDepotipi(hareketSicilDTO.getDepotipi());
        cariDTO.setEvrakno(hareketSicilDTO.getEvrakno());
        cariDTO.setTipi(hareketSicilDTO.getEvraktipi());
        cariDTO.setTarih(hareketSicilDTO.getTarihi());
        cariDTO.setVade(hareketSicilDTO.getTarihi().plusDays(hareketSicilDTO.getGun()));
        cariDTO.setAciklama(aciklama);
        cariDTO.setBorc(BigDecimal.ZERO);
        cariDTO.setAlacak(BigDecimal.ZERO);
        if (
            hareketSicilDTO.getEvraktipi() == EvrakTipi.IRSALIYELI_SATIS_FATURASI ||
            hareketSicilDTO.getEvraktipi() == EvrakTipi.SATIS_FATURASI
        ) {
            cariDTO.setBorc(hareketSicilDTO.getTutari());
        } else {
            cariDTO.setAlacak(hareketSicilDTO.getTutari());
        }

        hareketSicilDTO.setCari(cariDTO);
    }

    @Override
    public HareketSicilDTO save(HareketSicilDTO hareketSicilDTO) {
        log.debug("Request to save HareketSicil : {}", hareketSicilDTO);

        createOrUpdateCari(hareketSicilDTO);
        HareketSicil hareketSicil = hareketSicilMapper.toEntity(hareketSicilDTO);
        hareketSicil = hareketSicilRepository.save(hareketSicil);
        return hareketSicilMapper.toDto(hareketSicil);
    }

    @Override
    public HareketSicilDTO update(HareketSicilDTO hareketSicilDTO) {
        log.debug("Request to update HareketSicil : {}", hareketSicilDTO);

        createOrUpdateCari(hareketSicilDTO);
        HareketSicil hareketSicil = hareketSicilMapper.toEntity(hareketSicilDTO);
        hareketSicil = hareketSicilRepository.save(hareketSicil);
        return hareketSicilMapper.toDto(hareketSicil);
    }

    @Override
    public Optional<HareketSicilDTO> partialUpdate(HareketSicilDTO hareketSicilDTO) {
        log.debug("Request to partially update HareketSicil : {}", hareketSicilDTO);

        createOrUpdateCari(hareketSicilDTO);
        return hareketSicilRepository
            .findById(hareketSicilDTO.getId())
            .map(existingHareketSicil -> {
                hareketSicilMapper.partialUpdate(existingHareketSicil, hareketSicilDTO);

                return existingHareketSicil;
            })
            .map(hareketSicilRepository::save)
            .map(hareketSicilMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HareketSicilDTO> findAll(Pageable pageable) {
        log.debug("Request to get all HareketSicils");
        return hareketSicilRepository.findAll(pageable).map(hareketSicilMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HareketSicilDTO> findOne(Long id) {
        log.debug("Request to get HareketSicil : {}", id);
        return hareketSicilRepository.findById(id).map(hareketSicilMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete HareketSicil : {}", id);
        hareketSicilRepository.deleteById(id);
    }

    public String generateEvrakNo(EvrakTipi evrakTipi, DepoTipi depoTipi, LocalDate tarihi) {
        if (evrakTipi == null || depoTipi == null || tarihi == null) return "";

        return hareketSicilRepository
            .findMaxEvrakNo(evrakTipi, depoTipi, LocalDate.of(tarihi.getYear(), 1, 1), LocalDate.of(tarihi.getYear(), 12, 31))
            .filter(m -> m.matches("(DZ)\\d{4}\\d{1}\\d+"))
            .map(m -> m.substring(0, 8) + String.format("%06d", Integer.parseInt(m.substring(8)) + 1))
            .orElse(String.format("%s%04d%s%06d", "DZ", tarihi.getYear(), depoTipi == DepoTipi.DEPO1 ? "0" : "9", 1));
    }
}
