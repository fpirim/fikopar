package com.dizel.fikopar.service.impl;

import com.dizel.fikopar.domain.Katalog;
import com.dizel.fikopar.repository.KatalogRepository;
import com.dizel.fikopar.service.KatalogService;
import com.dizel.fikopar.service.dto.KatalogDTO;
import com.dizel.fikopar.service.mapper.KatalogMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Katalog}.
 */
@Service
@Transactional
public class KatalogServiceImpl implements KatalogService {

    private final Logger log = LoggerFactory.getLogger(KatalogServiceImpl.class);

    private final KatalogRepository katalogRepository;

    private final KatalogMapper katalogMapper;

    public KatalogServiceImpl(KatalogRepository katalogRepository, KatalogMapper katalogMapper) {
        this.katalogRepository = katalogRepository;
        this.katalogMapper = katalogMapper;
    }

    @Override
    public KatalogDTO save(KatalogDTO katalogDTO) {
        log.debug("Request to save Katalog : {}", katalogDTO);
        Katalog katalog = katalogMapper.toEntity(katalogDTO);
        katalog = katalogRepository.save(katalog);
        return katalogMapper.toDto(katalog);
    }

    @Override
    public KatalogDTO update(KatalogDTO katalogDTO) {
        log.debug("Request to update Katalog : {}", katalogDTO);
        Katalog katalog = katalogMapper.toEntity(katalogDTO);
        katalog = katalogRepository.save(katalog);
        return katalogMapper.toDto(katalog);
    }

    @Override
    public Optional<KatalogDTO> partialUpdate(KatalogDTO katalogDTO) {
        log.debug("Request to partially update Katalog : {}", katalogDTO);

        return katalogRepository
            .findById(katalogDTO.getId())
            .map(existingKatalog -> {
                katalogMapper.partialUpdate(existingKatalog, katalogDTO);

                return existingKatalog;
            })
            .map(katalogRepository::save)
            .map(katalogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KatalogDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Katalogs");
        return katalogRepository.findAll(pageable).map(katalogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KatalogDTO> findOne(Long id) {
        log.debug("Request to get Katalog : {}", id);
        return katalogRepository.findById(id).map(katalogMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Katalog : {}", id);
        katalogRepository.deleteById(id);
    }
}
