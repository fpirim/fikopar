package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.CariDevir;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.repository.CariDevirRepository;
import com.dizel.fikopar.service.criteria.CariDevirCriteria;
import com.dizel.fikopar.service.dto.CariDevirDTO;
import com.dizel.fikopar.service.mapper.CariDevirMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CariDevirResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CariDevirResourceIT {

    private static final DepoTipi DEFAULT_DEPOTIPI = DepoTipi.DEPO1;
    private static final DepoTipi UPDATED_DEPOTIPI = DepoTipi.DEPO2;

    private static final LocalDate DEFAULT_YIL = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_YIL = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_YIL = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_BORC = new BigDecimal(1);
    private static final BigDecimal UPDATED_BORC = new BigDecimal(2);
    private static final BigDecimal SMALLER_BORC = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_ALACAK = new BigDecimal(1);
    private static final BigDecimal UPDATED_ALACAK = new BigDecimal(2);
    private static final BigDecimal SMALLER_ALACAK = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/cari-devirs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CariDevirRepository cariDevirRepository;

    @Autowired
    private CariDevirMapper cariDevirMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCariDevirMockMvc;

    private CariDevir cariDevir;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CariDevir createEntity(EntityManager em) {
        CariDevir cariDevir = new CariDevir().depotipi(DEFAULT_DEPOTIPI).yil(DEFAULT_YIL).borc(DEFAULT_BORC).alacak(DEFAULT_ALACAK);
        return cariDevir;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CariDevir createUpdatedEntity(EntityManager em) {
        CariDevir cariDevir = new CariDevir().depotipi(UPDATED_DEPOTIPI).yil(UPDATED_YIL).borc(UPDATED_BORC).alacak(UPDATED_ALACAK);
        return cariDevir;
    }

    @BeforeEach
    public void initTest() {
        cariDevir = createEntity(em);
    }

    @Test
    @Transactional
    void createCariDevir() throws Exception {
        int databaseSizeBeforeCreate = cariDevirRepository.findAll().size();
        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);
        restCariDevirMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDevirDTO)))
            .andExpect(status().isCreated());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeCreate + 1);
        CariDevir testCariDevir = cariDevirList.get(cariDevirList.size() - 1);
        assertThat(testCariDevir.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testCariDevir.getYil()).isEqualTo(DEFAULT_YIL);
        assertThat(testCariDevir.getBorc()).isEqualByComparingTo(DEFAULT_BORC);
        assertThat(testCariDevir.getAlacak()).isEqualByComparingTo(DEFAULT_ALACAK);
    }

    @Test
    @Transactional
    void createCariDevirWithExistingId() throws Exception {
        // Create the CariDevir with an existing ID
        cariDevir.setId(1L);
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        int databaseSizeBeforeCreate = cariDevirRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCariDevirMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDevirDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCariDevirs() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cariDevir.getId().intValue())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].yil").value(hasItem(DEFAULT_YIL.toString())))
            .andExpect(jsonPath("$.[*].borc").value(hasItem(sameNumber(DEFAULT_BORC))))
            .andExpect(jsonPath("$.[*].alacak").value(hasItem(sameNumber(DEFAULT_ALACAK))));
    }

    @Test
    @Transactional
    void getCariDevir() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get the cariDevir
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL_ID, cariDevir.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cariDevir.getId().intValue()))
            .andExpect(jsonPath("$.depotipi").value(DEFAULT_DEPOTIPI.toString()))
            .andExpect(jsonPath("$.yil").value(DEFAULT_YIL.toString()))
            .andExpect(jsonPath("$.borc").value(sameNumber(DEFAULT_BORC)))
            .andExpect(jsonPath("$.alacak").value(sameNumber(DEFAULT_ALACAK)));
    }

    @Test
    @Transactional
    void getCariDevirsByIdFiltering() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        Long id = cariDevir.getId();

        defaultCariDevirShouldBeFound("id.equals=" + id);
        defaultCariDevirShouldNotBeFound("id.notEquals=" + id);

        defaultCariDevirShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCariDevirShouldNotBeFound("id.greaterThan=" + id);

        defaultCariDevirShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCariDevirShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCariDevirsByDepotipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where depotipi equals to DEFAULT_DEPOTIPI
        defaultCariDevirShouldBeFound("depotipi.equals=" + DEFAULT_DEPOTIPI);

        // Get all the cariDevirList where depotipi equals to UPDATED_DEPOTIPI
        defaultCariDevirShouldNotBeFound("depotipi.equals=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCariDevirsByDepotipiIsInShouldWork() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where depotipi in DEFAULT_DEPOTIPI or UPDATED_DEPOTIPI
        defaultCariDevirShouldBeFound("depotipi.in=" + DEFAULT_DEPOTIPI + "," + UPDATED_DEPOTIPI);

        // Get all the cariDevirList where depotipi equals to UPDATED_DEPOTIPI
        defaultCariDevirShouldNotBeFound("depotipi.in=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCariDevirsByDepotipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where depotipi is not null
        defaultCariDevirShouldBeFound("depotipi.specified=true");

        // Get all the cariDevirList where depotipi is null
        defaultCariDevirShouldNotBeFound("depotipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil equals to DEFAULT_YIL
        defaultCariDevirShouldBeFound("yil.equals=" + DEFAULT_YIL);

        // Get all the cariDevirList where yil equals to UPDATED_YIL
        defaultCariDevirShouldNotBeFound("yil.equals=" + UPDATED_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsInShouldWork() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil in DEFAULT_YIL or UPDATED_YIL
        defaultCariDevirShouldBeFound("yil.in=" + DEFAULT_YIL + "," + UPDATED_YIL);

        // Get all the cariDevirList where yil equals to UPDATED_YIL
        defaultCariDevirShouldNotBeFound("yil.in=" + UPDATED_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil is not null
        defaultCariDevirShouldBeFound("yil.specified=true");

        // Get all the cariDevirList where yil is null
        defaultCariDevirShouldNotBeFound("yil.specified=false");
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil is greater than or equal to DEFAULT_YIL
        defaultCariDevirShouldBeFound("yil.greaterThanOrEqual=" + DEFAULT_YIL);

        // Get all the cariDevirList where yil is greater than or equal to UPDATED_YIL
        defaultCariDevirShouldNotBeFound("yil.greaterThanOrEqual=" + UPDATED_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil is less than or equal to DEFAULT_YIL
        defaultCariDevirShouldBeFound("yil.lessThanOrEqual=" + DEFAULT_YIL);

        // Get all the cariDevirList where yil is less than or equal to SMALLER_YIL
        defaultCariDevirShouldNotBeFound("yil.lessThanOrEqual=" + SMALLER_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsLessThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil is less than DEFAULT_YIL
        defaultCariDevirShouldNotBeFound("yil.lessThan=" + DEFAULT_YIL);

        // Get all the cariDevirList where yil is less than UPDATED_YIL
        defaultCariDevirShouldBeFound("yil.lessThan=" + UPDATED_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByYilIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where yil is greater than DEFAULT_YIL
        defaultCariDevirShouldNotBeFound("yil.greaterThan=" + DEFAULT_YIL);

        // Get all the cariDevirList where yil is greater than SMALLER_YIL
        defaultCariDevirShouldBeFound("yil.greaterThan=" + SMALLER_YIL);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc equals to DEFAULT_BORC
        defaultCariDevirShouldBeFound("borc.equals=" + DEFAULT_BORC);

        // Get all the cariDevirList where borc equals to UPDATED_BORC
        defaultCariDevirShouldNotBeFound("borc.equals=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsInShouldWork() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc in DEFAULT_BORC or UPDATED_BORC
        defaultCariDevirShouldBeFound("borc.in=" + DEFAULT_BORC + "," + UPDATED_BORC);

        // Get all the cariDevirList where borc equals to UPDATED_BORC
        defaultCariDevirShouldNotBeFound("borc.in=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc is not null
        defaultCariDevirShouldBeFound("borc.specified=true");

        // Get all the cariDevirList where borc is null
        defaultCariDevirShouldNotBeFound("borc.specified=false");
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc is greater than or equal to DEFAULT_BORC
        defaultCariDevirShouldBeFound("borc.greaterThanOrEqual=" + DEFAULT_BORC);

        // Get all the cariDevirList where borc is greater than or equal to UPDATED_BORC
        defaultCariDevirShouldNotBeFound("borc.greaterThanOrEqual=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc is less than or equal to DEFAULT_BORC
        defaultCariDevirShouldBeFound("borc.lessThanOrEqual=" + DEFAULT_BORC);

        // Get all the cariDevirList where borc is less than or equal to SMALLER_BORC
        defaultCariDevirShouldNotBeFound("borc.lessThanOrEqual=" + SMALLER_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsLessThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc is less than DEFAULT_BORC
        defaultCariDevirShouldNotBeFound("borc.lessThan=" + DEFAULT_BORC);

        // Get all the cariDevirList where borc is less than UPDATED_BORC
        defaultCariDevirShouldBeFound("borc.lessThan=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByBorcIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where borc is greater than DEFAULT_BORC
        defaultCariDevirShouldNotBeFound("borc.greaterThan=" + DEFAULT_BORC);

        // Get all the cariDevirList where borc is greater than SMALLER_BORC
        defaultCariDevirShouldBeFound("borc.greaterThan=" + SMALLER_BORC);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak equals to DEFAULT_ALACAK
        defaultCariDevirShouldBeFound("alacak.equals=" + DEFAULT_ALACAK);

        // Get all the cariDevirList where alacak equals to UPDATED_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.equals=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsInShouldWork() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak in DEFAULT_ALACAK or UPDATED_ALACAK
        defaultCariDevirShouldBeFound("alacak.in=" + DEFAULT_ALACAK + "," + UPDATED_ALACAK);

        // Get all the cariDevirList where alacak equals to UPDATED_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.in=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak is not null
        defaultCariDevirShouldBeFound("alacak.specified=true");

        // Get all the cariDevirList where alacak is null
        defaultCariDevirShouldNotBeFound("alacak.specified=false");
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak is greater than or equal to DEFAULT_ALACAK
        defaultCariDevirShouldBeFound("alacak.greaterThanOrEqual=" + DEFAULT_ALACAK);

        // Get all the cariDevirList where alacak is greater than or equal to UPDATED_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.greaterThanOrEqual=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak is less than or equal to DEFAULT_ALACAK
        defaultCariDevirShouldBeFound("alacak.lessThanOrEqual=" + DEFAULT_ALACAK);

        // Get all the cariDevirList where alacak is less than or equal to SMALLER_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.lessThanOrEqual=" + SMALLER_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsLessThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak is less than DEFAULT_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.lessThan=" + DEFAULT_ALACAK);

        // Get all the cariDevirList where alacak is less than UPDATED_ALACAK
        defaultCariDevirShouldBeFound("alacak.lessThan=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByAlacakIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        // Get all the cariDevirList where alacak is greater than DEFAULT_ALACAK
        defaultCariDevirShouldNotBeFound("alacak.greaterThan=" + DEFAULT_ALACAK);

        // Get all the cariDevirList where alacak is greater than SMALLER_ALACAK
        defaultCariDevirShouldBeFound("alacak.greaterThan=" + SMALLER_ALACAK);
    }

    @Test
    @Transactional
    void getAllCariDevirsByCariIsEqualToSomething() throws Exception {
        Cari cari;
        if (TestUtil.findAll(em, Cari.class).isEmpty()) {
            cariDevirRepository.saveAndFlush(cariDevir);
            cari = CariResourceIT.createEntity(em);
        } else {
            cari = TestUtil.findAll(em, Cari.class).get(0);
        }
        em.persist(cari);
        em.flush();
        cariDevir.setCari(cari);
        cariDevirRepository.saveAndFlush(cariDevir);
        Long cariId = cari.getId();

        // Get all the cariDevirList where cari equals to cariId
        defaultCariDevirShouldBeFound("cariId.equals=" + cariId);

        // Get all the cariDevirList where cari equals to (cariId + 1)
        defaultCariDevirShouldNotBeFound("cariId.equals=" + (cariId + 1));
    }

    @Test
    @Transactional
    void getAllCariDevirsBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            cariDevirRepository.saveAndFlush(cariDevir);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        cariDevir.setSicil(sicil);
        cariDevirRepository.saveAndFlush(cariDevir);
        Long sicilId = sicil.getId();

        // Get all the cariDevirList where sicil equals to sicilId
        defaultCariDevirShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the cariDevirList where sicil equals to (sicilId + 1)
        defaultCariDevirShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCariDevirShouldBeFound(String filter) throws Exception {
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cariDevir.getId().intValue())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].yil").value(hasItem(DEFAULT_YIL.toString())))
            .andExpect(jsonPath("$.[*].borc").value(hasItem(sameNumber(DEFAULT_BORC))))
            .andExpect(jsonPath("$.[*].alacak").value(hasItem(sameNumber(DEFAULT_ALACAK))));

        // Check, that the count call also returns 1
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCariDevirShouldNotBeFound(String filter) throws Exception {
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCariDevirMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCariDevir() throws Exception {
        // Get the cariDevir
        restCariDevirMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCariDevir() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();

        // Update the cariDevir
        CariDevir updatedCariDevir = cariDevirRepository.findById(cariDevir.getId()).get();
        // Disconnect from session so that the updates on updatedCariDevir are not directly saved in db
        em.detach(updatedCariDevir);
        updatedCariDevir.depotipi(UPDATED_DEPOTIPI).yil(UPDATED_YIL).borc(UPDATED_BORC).alacak(UPDATED_ALACAK);
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(updatedCariDevir);

        restCariDevirMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cariDevirDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isOk());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
        CariDevir testCariDevir = cariDevirList.get(cariDevirList.size() - 1);
        assertThat(testCariDevir.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCariDevir.getYil()).isEqualTo(UPDATED_YIL);
        assertThat(testCariDevir.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCariDevir.getAlacak()).isEqualByComparingTo(UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void putNonExistingCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cariDevirDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDevirDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCariDevirWithPatch() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();

        // Update the cariDevir using partial update
        CariDevir partialUpdatedCariDevir = new CariDevir();
        partialUpdatedCariDevir.setId(cariDevir.getId());

        partialUpdatedCariDevir.borc(UPDATED_BORC);

        restCariDevirMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCariDevir.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCariDevir))
            )
            .andExpect(status().isOk());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
        CariDevir testCariDevir = cariDevirList.get(cariDevirList.size() - 1);
        assertThat(testCariDevir.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testCariDevir.getYil()).isEqualTo(DEFAULT_YIL);
        assertThat(testCariDevir.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCariDevir.getAlacak()).isEqualByComparingTo(DEFAULT_ALACAK);
    }

    @Test
    @Transactional
    void fullUpdateCariDevirWithPatch() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();

        // Update the cariDevir using partial update
        CariDevir partialUpdatedCariDevir = new CariDevir();
        partialUpdatedCariDevir.setId(cariDevir.getId());

        partialUpdatedCariDevir.depotipi(UPDATED_DEPOTIPI).yil(UPDATED_YIL).borc(UPDATED_BORC).alacak(UPDATED_ALACAK);

        restCariDevirMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCariDevir.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCariDevir))
            )
            .andExpect(status().isOk());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
        CariDevir testCariDevir = cariDevirList.get(cariDevirList.size() - 1);
        assertThat(testCariDevir.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCariDevir.getYil()).isEqualTo(UPDATED_YIL);
        assertThat(testCariDevir.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCariDevir.getAlacak()).isEqualByComparingTo(UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void patchNonExistingCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cariDevirDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCariDevir() throws Exception {
        int databaseSizeBeforeUpdate = cariDevirRepository.findAll().size();
        cariDevir.setId(count.incrementAndGet());

        // Create the CariDevir
        CariDevirDTO cariDevirDTO = cariDevirMapper.toDto(cariDevir);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariDevirMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cariDevirDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CariDevir in the database
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCariDevir() throws Exception {
        // Initialize the database
        cariDevirRepository.saveAndFlush(cariDevir);

        int databaseSizeBeforeDelete = cariDevirRepository.findAll().size();

        // Delete the cariDevir
        restCariDevirMockMvc
            .perform(delete(ENTITY_API_URL_ID, cariDevir.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CariDevir> cariDevirList = cariDevirRepository.findAll();
        assertThat(cariDevirList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
