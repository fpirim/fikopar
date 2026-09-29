package dev.fikopar.service.impl;

import dev.fikopar.domain.Cari;
import dev.fikopar.repository.CariRepository;
import dev.fikopar.service.CariService;
import dev.fikopar.service.dto.CariDTO;
import dev.fikopar.service.mapper.CariMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Cari}.
 */
@Service
@Transactional
public class CariServiceImpl implements CariService {

    private final Logger log = LoggerFactory.getLogger(CariServiceImpl.class);

    private final CariRepository cariRepository;

    private final CariMapper cariMapper;

    public CariServiceImpl(CariRepository cariRepository, CariMapper cariMapper) {
        this.cariRepository = cariRepository;
        this.cariMapper = cariMapper;
    }

    @Override
    public CariDTO save(CariDTO cariDTO) {
        log.debug("Request to save Cari : {}", cariDTO);
        Cari cari = cariMapper.toEntity(cariDTO);
        cari = cariRepository.save(cari);
        return cariMapper.toDto(cari);
    }

    @Override
    public CariDTO update(CariDTO cariDTO) {
        log.debug("Request to update Cari : {}", cariDTO);
        Cari cari = cariMapper.toEntity(cariDTO);
        cari = cariRepository.save(cari);
        return cariMapper.toDto(cari);
    }

    @Override
    public Optional<CariDTO> partialUpdate(CariDTO cariDTO) {
        log.debug("Request to partially update Cari : {}", cariDTO);

        return cariRepository
            .findById(cariDTO.getId())
            .map(existingCari -> {
                cariMapper.partialUpdate(existingCari, cariDTO);

                return existingCari;
            })
            .map(cariRepository::save)
            .map(cariMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CariDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Caris");
        return cariRepository.findAll(pageable).map(cariMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CariDTO> findOne(Long id) {
        log.debug("Request to get Cari : {}", id);
        return cariRepository.findById(id).map(cariMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Cari : {}", id);
        cariRepository.deleteById(id);
    }
}
