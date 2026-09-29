package dev.fikopar.service.impl;

import dev.fikopar.domain.CariDevir;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.repository.CariDevirRepository;
import dev.fikopar.service.CariDevirService;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.dto.CariDevirDTO;
import dev.fikopar.service.mapper.CariDevirMapper;
import dev.fikopar.service.mapper.CariMapper;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CariDevir}.
 */
@Service
@Transactional
public class CariDevirServiceImpl implements CariDevirService {

    private final Logger log = LoggerFactory.getLogger(CariDevirServiceImpl.class);

    private final CariDevirRepository cariDevirRepository;

    private final CariDevirMapper cariDevirMapper;

    private final CariMapper cariMapper;

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("YYYY");

    public CariDevirServiceImpl(CariDevirRepository cariDevirRepository, CariDevirMapper cariDevirMapper, CariMapper cariMapper) {
        this.cariDevirRepository = cariDevirRepository;
        this.cariDevirMapper = cariDevirMapper;
        this.cariMapper = cariMapper;
    }

    private void createOrUpdateCari(CariDevirDTO cariDevirDTO) {
        CariDTO cariDTO;
        if (cariDevirDTO.getId() == null) {
            cariDTO = new CariDTO();
        } else {
            cariDTO = cariDevirRepository.findById(cariDevirDTO.getId()).map(c -> cariMapper.toDto(c.getCari())).orElse(new CariDTO());
        }

        cariDTO.setSicil(cariDevirDTO.getSicil());
        cariDTO.setDepotipi(cariDevirDTO.getDepotipi());
        cariDTO.setEvrakno(cariDevirDTO.getYil().format(YEAR_FORMATTER));
        cariDTO.setTipi(EvrakTipi.DEVIR);
        cariDTO.setTarih(cariDevirDTO.getYil());
        cariDTO.setVade(cariDevirDTO.getYil());
        cariDTO.setAciklama(cariDevirDTO.getYil() + " yılından devir");
        cariDTO.setAlacak(cariDevirDTO.getAlacak());
        cariDTO.setBorc(cariDevirDTO.getBorc());
        cariDevirDTO.setCari(cariDTO);
    }

    @Override
    @Transactional
    public CariDevirDTO save(CariDevirDTO cariDevirDTO) {
        log.debug("Request to save CariDevir : {}", cariDevirDTO);

        createOrUpdateCari(cariDevirDTO);
        CariDevir cariDevir = cariDevirMapper.toEntity(cariDevirDTO);
        cariDevir = cariDevirRepository.save(cariDevir);

        return cariDevirMapper.toDto(cariDevir);
    }

    @Override
    public CariDevirDTO update(CariDevirDTO cariDevirDTO) {
        log.debug("Request to update CariDevir : {}", cariDevirDTO);

        createOrUpdateCari(cariDevirDTO);
        CariDevir cariDevir = cariDevirMapper.toEntity(cariDevirDTO);
        cariDevir = cariDevirRepository.save(cariDevir);
        return cariDevirMapper.toDto(cariDevir);
    }

    @Override
    public Optional<CariDevirDTO> partialUpdate(CariDevirDTO cariDevirDTO) {
        log.debug("Request to partially update CariDevir : {}", cariDevirDTO);

        createOrUpdateCari(cariDevirDTO);
        return cariDevirRepository
            .findById(cariDevirDTO.getId())
            .map(existingCariDevir -> {
                cariDevirMapper.partialUpdate(existingCariDevir, cariDevirDTO);
                return existingCariDevir;
            })
            .map(cariDevirRepository::save)
            .map(cariDevirMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CariDevirDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CariDevirs");
        return cariDevirRepository.findAll(pageable).map(cariDevirMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CariDevirDTO> findOne(Long id) {
        log.debug("Request to get CariDevir : {}", id);
        return cariDevirRepository.findById(id).map(cariDevirMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CariDevir : {}", id);
        cariDevirRepository.deleteById(id);
    }
}
