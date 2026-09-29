package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.CekSenet;
import com.dizel.fikopar.domain.CekSenetDetay;
import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.domain.enumeration.IslemTipi;
import com.dizel.fikopar.repository.CekSenetDetayRepository;
import com.dizel.fikopar.service.criteria.CekSenetDetayCriteria;
import com.dizel.fikopar.service.dto.CekSenetDetayDTO;
import com.dizel.fikopar.service.mapper.CekSenetDetayMapper;
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
 * Integration tests for the {@link CekSenetDetayResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CekSenetDetayResourceIT {

    private static final IslemTipi DEFAULT_ISLEMTIPI = IslemTipi.PORTFOY;
    private static final IslemTipi UPDATED_ISLEMTIPI = IslemTipi.BANKAYA_VERME;

    private static final LocalDate DEFAULT_TARIHI = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TARIHI = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TARIHI = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_TUTAR = new BigDecimal(1);
    private static final BigDecimal UPDATED_TUTAR = new BigDecimal(2);
    private static final BigDecimal SMALLER_TUTAR = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/cek-senet-detays";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CekSenetDetayRepository cekSenetDetayRepository;

    @Autowired
    private CekSenetDetayMapper cekSenetDetayMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCekSenetDetayMockMvc;

    private CekSenetDetay cekSenetDetay;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CekSenetDetay createEntity(EntityManager em) {
        CekSenetDetay cekSenetDetay = new CekSenetDetay().islemtipi(DEFAULT_ISLEMTIPI).tarihi(DEFAULT_TARIHI).tutar(DEFAULT_TUTAR);
        return cekSenetDetay;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CekSenetDetay createUpdatedEntity(EntityManager em) {
        CekSenetDetay cekSenetDetay = new CekSenetDetay().islemtipi(UPDATED_ISLEMTIPI).tarihi(UPDATED_TARIHI).tutar(UPDATED_TUTAR);
        return cekSenetDetay;
    }

    @BeforeEach
    public void initTest() {
        cekSenetDetay = createEntity(em);
    }

    @Test
    @Transactional
    void createCekSenetDetay() throws Exception {
        int databaseSizeBeforeCreate = cekSenetDetayRepository.findAll().size();
        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);
        restCekSenetDetayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isCreated());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeCreate + 1);
        CekSenetDetay testCekSenetDetay = cekSenetDetayList.get(cekSenetDetayList.size() - 1);
        assertThat(testCekSenetDetay.getIslemtipi()).isEqualTo(DEFAULT_ISLEMTIPI);
        assertThat(testCekSenetDetay.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testCekSenetDetay.getTutar()).isEqualByComparingTo(DEFAULT_TUTAR);
    }

    @Test
    @Transactional
    void createCekSenetDetayWithExistingId() throws Exception {
        // Create the CekSenetDetay with an existing ID
        cekSenetDetay.setId(1L);
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        int databaseSizeBeforeCreate = cekSenetDetayRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCekSenetDetayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCekSenetDetays() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cekSenetDetay.getId().intValue())))
            .andExpect(jsonPath("$.[*].islemtipi").value(hasItem(DEFAULT_ISLEMTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].tutar").value(hasItem(sameNumber(DEFAULT_TUTAR))));
    }

    @Test
    @Transactional
    void getCekSenetDetay() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get the cekSenetDetay
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL_ID, cekSenetDetay.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cekSenetDetay.getId().intValue()))
            .andExpect(jsonPath("$.islemtipi").value(DEFAULT_ISLEMTIPI.toString()))
            .andExpect(jsonPath("$.tarihi").value(DEFAULT_TARIHI.toString()))
            .andExpect(jsonPath("$.tutar").value(sameNumber(DEFAULT_TUTAR)));
    }

    @Test
    @Transactional
    void getCekSenetDetaysByIdFiltering() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        Long id = cekSenetDetay.getId();

        defaultCekSenetDetayShouldBeFound("id.equals=" + id);
        defaultCekSenetDetayShouldNotBeFound("id.notEquals=" + id);

        defaultCekSenetDetayShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCekSenetDetayShouldNotBeFound("id.greaterThan=" + id);

        defaultCekSenetDetayShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCekSenetDetayShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByIslemtipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where islemtipi equals to DEFAULT_ISLEMTIPI
        defaultCekSenetDetayShouldBeFound("islemtipi.equals=" + DEFAULT_ISLEMTIPI);

        // Get all the cekSenetDetayList where islemtipi equals to UPDATED_ISLEMTIPI
        defaultCekSenetDetayShouldNotBeFound("islemtipi.equals=" + UPDATED_ISLEMTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByIslemtipiIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where islemtipi in DEFAULT_ISLEMTIPI or UPDATED_ISLEMTIPI
        defaultCekSenetDetayShouldBeFound("islemtipi.in=" + DEFAULT_ISLEMTIPI + "," + UPDATED_ISLEMTIPI);

        // Get all the cekSenetDetayList where islemtipi equals to UPDATED_ISLEMTIPI
        defaultCekSenetDetayShouldNotBeFound("islemtipi.in=" + UPDATED_ISLEMTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByIslemtipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where islemtipi is not null
        defaultCekSenetDetayShouldBeFound("islemtipi.specified=true");

        // Get all the cekSenetDetayList where islemtipi is null
        defaultCekSenetDetayShouldNotBeFound("islemtipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi equals to DEFAULT_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.equals=" + DEFAULT_TARIHI);

        // Get all the cekSenetDetayList where tarihi equals to UPDATED_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.equals=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi in DEFAULT_TARIHI or UPDATED_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.in=" + DEFAULT_TARIHI + "," + UPDATED_TARIHI);

        // Get all the cekSenetDetayList where tarihi equals to UPDATED_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.in=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi is not null
        defaultCekSenetDetayShouldBeFound("tarihi.specified=true");

        // Get all the cekSenetDetayList where tarihi is null
        defaultCekSenetDetayShouldNotBeFound("tarihi.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi is greater than or equal to DEFAULT_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.greaterThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the cekSenetDetayList where tarihi is greater than or equal to UPDATED_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.greaterThanOrEqual=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi is less than or equal to DEFAULT_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.lessThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the cekSenetDetayList where tarihi is less than or equal to SMALLER_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.lessThanOrEqual=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsLessThanSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi is less than DEFAULT_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.lessThan=" + DEFAULT_TARIHI);

        // Get all the cekSenetDetayList where tarihi is less than UPDATED_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.lessThan=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTarihiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tarihi is greater than DEFAULT_TARIHI
        defaultCekSenetDetayShouldNotBeFound("tarihi.greaterThan=" + DEFAULT_TARIHI);

        // Get all the cekSenetDetayList where tarihi is greater than SMALLER_TARIHI
        defaultCekSenetDetayShouldBeFound("tarihi.greaterThan=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar equals to DEFAULT_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.equals=" + DEFAULT_TUTAR);

        // Get all the cekSenetDetayList where tutar equals to UPDATED_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.equals=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar in DEFAULT_TUTAR or UPDATED_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.in=" + DEFAULT_TUTAR + "," + UPDATED_TUTAR);

        // Get all the cekSenetDetayList where tutar equals to UPDATED_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.in=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar is not null
        defaultCekSenetDetayShouldBeFound("tutar.specified=true");

        // Get all the cekSenetDetayList where tutar is null
        defaultCekSenetDetayShouldNotBeFound("tutar.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar is greater than or equal to DEFAULT_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.greaterThanOrEqual=" + DEFAULT_TUTAR);

        // Get all the cekSenetDetayList where tutar is greater than or equal to UPDATED_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.greaterThanOrEqual=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar is less than or equal to DEFAULT_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.lessThanOrEqual=" + DEFAULT_TUTAR);

        // Get all the cekSenetDetayList where tutar is less than or equal to SMALLER_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.lessThanOrEqual=" + SMALLER_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsLessThanSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar is less than DEFAULT_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.lessThan=" + DEFAULT_TUTAR);

        // Get all the cekSenetDetayList where tutar is less than UPDATED_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.lessThan=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByTutarIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        // Get all the cekSenetDetayList where tutar is greater than DEFAULT_TUTAR
        defaultCekSenetDetayShouldNotBeFound("tutar.greaterThan=" + DEFAULT_TUTAR);

        // Get all the cekSenetDetayList where tutar is greater than SMALLER_TUTAR
        defaultCekSenetDetayShouldBeFound("tutar.greaterThan=" + SMALLER_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByMakbuzIsEqualToSomething() throws Exception {
        Makbuz makbuz;
        if (TestUtil.findAll(em, Makbuz.class).isEmpty()) {
            cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
            makbuz = MakbuzResourceIT.createEntity(em);
        } else {
            makbuz = TestUtil.findAll(em, Makbuz.class).get(0);
        }
        em.persist(makbuz);
        em.flush();
        cekSenetDetay.setMakbuz(makbuz);
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
        Long makbuzId = makbuz.getId();

        // Get all the cekSenetDetayList where makbuz equals to makbuzId
        defaultCekSenetDetayShouldBeFound("makbuzId.equals=" + makbuzId);

        // Get all the cekSenetDetayList where makbuz equals to (makbuzId + 1)
        defaultCekSenetDetayShouldNotBeFound("makbuzId.equals=" + (makbuzId + 1));
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        cekSenetDetay.setSicil(sicil);
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
        Long sicilId = sicil.getId();

        // Get all the cekSenetDetayList where sicil equals to sicilId
        defaultCekSenetDetayShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the cekSenetDetayList where sicil equals to (sicilId + 1)
        defaultCekSenetDetayShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    @Test
    @Transactional
    void getAllCekSenetDetaysByCekSenetIsEqualToSomething() throws Exception {
        CekSenet cekSenet;
        if (TestUtil.findAll(em, CekSenet.class).isEmpty()) {
            cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
            cekSenet = CekSenetResourceIT.createEntity(em);
        } else {
            cekSenet = TestUtil.findAll(em, CekSenet.class).get(0);
        }
        em.persist(cekSenet);
        em.flush();
        cekSenetDetay.setCekSenet(cekSenet);
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);
        Long cekSenetId = cekSenet.getId();

        // Get all the cekSenetDetayList where cekSenet equals to cekSenetId
        defaultCekSenetDetayShouldBeFound("cekSenetId.equals=" + cekSenetId);

        // Get all the cekSenetDetayList where cekSenet equals to (cekSenetId + 1)
        defaultCekSenetDetayShouldNotBeFound("cekSenetId.equals=" + (cekSenetId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCekSenetDetayShouldBeFound(String filter) throws Exception {
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cekSenetDetay.getId().intValue())))
            .andExpect(jsonPath("$.[*].islemtipi").value(hasItem(DEFAULT_ISLEMTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].tutar").value(hasItem(sameNumber(DEFAULT_TUTAR))));

        // Check, that the count call also returns 1
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCekSenetDetayShouldNotBeFound(String filter) throws Exception {
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCekSenetDetayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCekSenetDetay() throws Exception {
        // Get the cekSenetDetay
        restCekSenetDetayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCekSenetDetay() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();

        // Update the cekSenetDetay
        CekSenetDetay updatedCekSenetDetay = cekSenetDetayRepository.findById(cekSenetDetay.getId()).get();
        // Disconnect from session so that the updates on updatedCekSenetDetay are not directly saved in db
        em.detach(updatedCekSenetDetay);
        updatedCekSenetDetay.islemtipi(UPDATED_ISLEMTIPI).tarihi(UPDATED_TARIHI).tutar(UPDATED_TUTAR);
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(updatedCekSenetDetay);

        restCekSenetDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cekSenetDetayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isOk());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
        CekSenetDetay testCekSenetDetay = cekSenetDetayList.get(cekSenetDetayList.size() - 1);
        assertThat(testCekSenetDetay.getIslemtipi()).isEqualTo(UPDATED_ISLEMTIPI);
        assertThat(testCekSenetDetay.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenetDetay.getTutar()).isEqualByComparingTo(UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void putNonExistingCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cekSenetDetayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCekSenetDetayWithPatch() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();

        // Update the cekSenetDetay using partial update
        CekSenetDetay partialUpdatedCekSenetDetay = new CekSenetDetay();
        partialUpdatedCekSenetDetay.setId(cekSenetDetay.getId());

        partialUpdatedCekSenetDetay.islemtipi(UPDATED_ISLEMTIPI).tarihi(UPDATED_TARIHI).tutar(UPDATED_TUTAR);

        restCekSenetDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCekSenetDetay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCekSenetDetay))
            )
            .andExpect(status().isOk());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
        CekSenetDetay testCekSenetDetay = cekSenetDetayList.get(cekSenetDetayList.size() - 1);
        assertThat(testCekSenetDetay.getIslemtipi()).isEqualTo(UPDATED_ISLEMTIPI);
        assertThat(testCekSenetDetay.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenetDetay.getTutar()).isEqualByComparingTo(UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void fullUpdateCekSenetDetayWithPatch() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();

        // Update the cekSenetDetay using partial update
        CekSenetDetay partialUpdatedCekSenetDetay = new CekSenetDetay();
        partialUpdatedCekSenetDetay.setId(cekSenetDetay.getId());

        partialUpdatedCekSenetDetay.islemtipi(UPDATED_ISLEMTIPI).tarihi(UPDATED_TARIHI).tutar(UPDATED_TUTAR);

        restCekSenetDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCekSenetDetay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCekSenetDetay))
            )
            .andExpect(status().isOk());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
        CekSenetDetay testCekSenetDetay = cekSenetDetayList.get(cekSenetDetayList.size() - 1);
        assertThat(testCekSenetDetay.getIslemtipi()).isEqualTo(UPDATED_ISLEMTIPI);
        assertThat(testCekSenetDetay.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenetDetay.getTutar()).isEqualByComparingTo(UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void patchNonExistingCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cekSenetDetayDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCekSenetDetay() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetDetayRepository.findAll().size();
        cekSenetDetay.setId(count.incrementAndGet());

        // Create the CekSenetDetay
        CekSenetDetayDTO cekSenetDetayDTO = cekSenetDetayMapper.toDto(cekSenetDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetDetayMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDetayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CekSenetDetay in the database
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCekSenetDetay() throws Exception {
        // Initialize the database
        cekSenetDetayRepository.saveAndFlush(cekSenetDetay);

        int databaseSizeBeforeDelete = cekSenetDetayRepository.findAll().size();

        // Delete the cekSenetDetay
        restCekSenetDetayMockMvc
            .perform(delete(ENTITY_API_URL_ID, cekSenetDetay.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CekSenetDetay> cekSenetDetayList = cekSenetDetayRepository.findAll();
        assertThat(cekSenetDetayList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
