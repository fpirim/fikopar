package dev.fikopar.service.impl;

import dev.fikopar.domain.Stok;
import dev.fikopar.repository.StokRepository;
import dev.fikopar.service.StokService;
import dev.fikopar.service.dto.StokDTO;
import dev.fikopar.service.mapper.StokMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Stok}.
 */
@Service
@Transactional
public class StokServiceImpl implements StokService {

    private final Logger log = LoggerFactory.getLogger(StokServiceImpl.class);

    private final StokRepository stokRepository;

    private final StokMapper stokMapper;

    public StokServiceImpl(StokRepository stokRepository, StokMapper stokMapper) {
        this.stokRepository = stokRepository;
        this.stokMapper = stokMapper;
    }

    @Override
    public StokDTO save(StokDTO stokDTO) {
        log.debug("Request to save Stok : {}", stokDTO);
        Stok stok = stokMapper.toEntity(stokDTO);
        stok = stokRepository.save(stok);
        return stokMapper.toDto(stok);
    }

    @Override
    public StokDTO update(StokDTO stokDTO) {
        log.debug("Request to update Stok : {}", stokDTO);
        Stok stok = stokMapper.toEntity(stokDTO);
        stok = stokRepository.save(stok);
        return stokMapper.toDto(stok);
    }

    @Override
    public Optional<StokDTO> partialUpdate(StokDTO stokDTO) {
        log.debug("Request to partially update Stok : {}", stokDTO);

        return stokRepository
            .findById(stokDTO.getId())
            .map(existingStok -> {
                stokMapper.partialUpdate(existingStok, stokDTO);

                return existingStok;
            })
            .map(stokRepository::save)
            .map(stokMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StokDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Stoks");
        return stokRepository.findAll(pageable).map(stokMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StokDTO> findOne(Long id) {
        log.debug("Request to get Stok : {}", id);
        return stokRepository.findById(id).map(stokMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Stok : {}", id);
        stokRepository.deleteById(id);
    }
}
