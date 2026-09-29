package com.dizel.fikopar.service.impl;

import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.repository.SicilRepository;
import com.dizel.fikopar.service.SicilService;
import com.dizel.fikopar.service.dto.SicilDTO;
import com.dizel.fikopar.service.mapper.SicilMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Sicil}.
 */
@Service
@Transactional
public class SicilServiceImpl implements SicilService {

    private final Logger log = LoggerFactory.getLogger(SicilServiceImpl.class);

    private final SicilRepository sicilRepository;

    private final SicilMapper sicilMapper;

    public SicilServiceImpl(SicilRepository sicilRepository, SicilMapper sicilMapper) {
        this.sicilRepository = sicilRepository;
        this.sicilMapper = sicilMapper;
    }

    @Override
    public SicilDTO save(SicilDTO sicilDTO) {
        log.debug("Request to save Sicil : {}", sicilDTO);
        Sicil sicil = sicilMapper.toEntity(sicilDTO);
        sicil = sicilRepository.save(sicil);
        return sicilMapper.toDto(sicil);
    }

    @Override
    public SicilDTO update(SicilDTO sicilDTO) {
        log.debug("Request to update Sicil : {}", sicilDTO);
        Sicil sicil = sicilMapper.toEntity(sicilDTO);
        sicil = sicilRepository.save(sicil);
        return sicilMapper.toDto(sicil);
    }

    @Override
    public Optional<SicilDTO> partialUpdate(SicilDTO sicilDTO) {
        log.debug("Request to partially update Sicil : {}", sicilDTO);

        return sicilRepository
            .findById(sicilDTO.getId())
            .map(existingSicil -> {
                sicilMapper.partialUpdate(existingSicil, sicilDTO);

                return existingSicil;
            })
            .map(sicilRepository::save)
            .map(sicilMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SicilDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Sicils");
        return sicilRepository.findAll(pageable).map(sicilMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SicilDTO> findOne(Long id) {
        log.debug("Request to get Sicil : {}", id);
        return sicilRepository.findById(id).map(sicilMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Sicil : {}", id);
        sicilRepository.deleteById(id);
    }
}
