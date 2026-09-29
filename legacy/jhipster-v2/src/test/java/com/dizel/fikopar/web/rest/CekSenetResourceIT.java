package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.CekSenet;
import com.dizel.fikopar.domain.CekSenetDetay;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.CekSenetRepository;
import com.dizel.fikopar.service.criteria.CekSenetCriteria;
import com.dizel.fikopar.service.dto.CekSenetDTO;
import com.dizel.fikopar.service.mapper.CekSenetMapper;
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
 * Integration tests for the {@link CekSenetResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CekSenetResourceIT {

    private static final String DEFAULT_CEKSENETNO = "AAAAAAAAAA";
    private static final String UPDATED_CEKSENETNO = "BBBBBBBBBB";

    private static final EvrakTipi DEFAULT_EVRAKTIPI = EvrakTipi.IRSALIYELI_SATIS_FATURASI;
    private static final EvrakTipi UPDATED_EVRAKTIPI = EvrakTipi.SATIS_FATURASI;

    private static final DepoTipi DEFAULT_DEPOTIPI = DepoTipi.DEPO1;
    private static final DepoTipi UPDATED_DEPOTIPI = DepoTipi.DEPO2;

    private static final LocalDate DEFAULT_TARIHI = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TARIHI = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TARIHI = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_VADE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VADE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_VADE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_ACIKLAMA = "AAAAAAAAAA";
    private static final String UPDATED_ACIKLAMA = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TUTAR = new BigDecimal(1);
    private static final BigDecimal UPDATED_TUTAR = new BigDecimal(2);
    private static final BigDecimal SMALLER_TUTAR = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/cek-senets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CekSenetRepository cekSenetRepository;

    @Autowired
    private CekSenetMapper cekSenetMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCekSenetMockMvc;

    private CekSenet cekSenet;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CekSenet createEntity(EntityManager em) {
        CekSenet cekSenet = new CekSenet()
            .ceksenetno(DEFAULT_CEKSENETNO)
            .evraktipi(DEFAULT_EVRAKTIPI)
            .depotipi(DEFAULT_DEPOTIPI)
            .tarihi(DEFAULT_TARIHI)
            .vade(DEFAULT_VADE)
            .aciklama(DEFAULT_ACIKLAMA)
            .tutar(DEFAULT_TUTAR);
        return cekSenet;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CekSenet createUpdatedEntity(EntityManager em) {
        CekSenet cekSenet = new CekSenet()
            .ceksenetno(UPDATED_CEKSENETNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutar(UPDATED_TUTAR);
        return cekSenet;
    }

    @BeforeEach
    public void initTest() {
        cekSenet = createEntity(em);
    }

    @Test
    @Transactional
    void createCekSenet() throws Exception {
        int databaseSizeBeforeCreate = cekSenetRepository.findAll().size();
        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);
        restCekSenetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDTO)))
            .andExpect(status().isCreated());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeCreate + 1);
        CekSenet testCekSenet = cekSenetList.get(cekSenetList.size() - 1);
        assertThat(testCekSenet.getCeksenetno()).isEqualTo(DEFAULT_CEKSENETNO);
        assertThat(testCekSenet.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testCekSenet.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testCekSenet.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testCekSenet.getVade()).isEqualTo(DEFAULT_VADE);
        assertThat(testCekSenet.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testCekSenet.getTutar()).isEqualByComparingTo(DEFAULT_TUTAR);
    }

    @Test
    @Transactional
    void createCekSenetWithExistingId() throws Exception {
        // Create the CekSenet with an existing ID
        cekSenet.setId(1L);
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        int databaseSizeBeforeCreate = cekSenetRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCekSenetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCekSenets() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cekSenet.getId().intValue())))
            .andExpect(jsonPath("$.[*].ceksenetno").value(hasItem(DEFAULT_CEKSENETNO)))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].tutar").value(hasItem(sameNumber(DEFAULT_TUTAR))));
    }

    @Test
    @Transactional
    void getCekSenet() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get the cekSenet
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL_ID, cekSenet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cekSenet.getId().intValue()))
            .andExpect(jsonPath("$.ceksenetno").value(DEFAULT_CEKSENETNO))
            .andExpect(jsonPath("$.evraktipi").value(DEFAULT_EVRAKTIPI.toString()))
            .andExpect(jsonPath("$.depotipi").value(DEFAULT_DEPOTIPI.toString()))
            .andExpect(jsonPath("$.tarihi").value(DEFAULT_TARIHI.toString()))
            .andExpect(jsonPath("$.vade").value(DEFAULT_VADE.toString()))
            .andExpect(jsonPath("$.aciklama").value(DEFAULT_ACIKLAMA))
            .andExpect(jsonPath("$.tutar").value(sameNumber(DEFAULT_TUTAR)));
    }

    @Test
    @Transactional
    void getCekSenetsByIdFiltering() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        Long id = cekSenet.getId();

        defaultCekSenetShouldBeFound("id.equals=" + id);
        defaultCekSenetShouldNotBeFound("id.notEquals=" + id);

        defaultCekSenetShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCekSenetShouldNotBeFound("id.greaterThan=" + id);

        defaultCekSenetShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCekSenetShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCekSenetsByCeksenetnoIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where ceksenetno equals to DEFAULT_CEKSENETNO
        defaultCekSenetShouldBeFound("ceksenetno.equals=" + DEFAULT_CEKSENETNO);

        // Get all the cekSenetList where ceksenetno equals to UPDATED_CEKSENETNO
        defaultCekSenetShouldNotBeFound("ceksenetno.equals=" + UPDATED_CEKSENETNO);
    }

    @Test
    @Transactional
    void getAllCekSenetsByCeksenetnoIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where ceksenetno in DEFAULT_CEKSENETNO or UPDATED_CEKSENETNO
        defaultCekSenetShouldBeFound("ceksenetno.in=" + DEFAULT_CEKSENETNO + "," + UPDATED_CEKSENETNO);

        // Get all the cekSenetList where ceksenetno equals to UPDATED_CEKSENETNO
        defaultCekSenetShouldNotBeFound("ceksenetno.in=" + UPDATED_CEKSENETNO);
    }

    @Test
    @Transactional
    void getAllCekSenetsByCeksenetnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where ceksenetno is not null
        defaultCekSenetShouldBeFound("ceksenetno.specified=true");

        // Get all the cekSenetList where ceksenetno is null
        defaultCekSenetShouldNotBeFound("ceksenetno.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByCeksenetnoContainsSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where ceksenetno contains DEFAULT_CEKSENETNO
        defaultCekSenetShouldBeFound("ceksenetno.contains=" + DEFAULT_CEKSENETNO);

        // Get all the cekSenetList where ceksenetno contains UPDATED_CEKSENETNO
        defaultCekSenetShouldNotBeFound("ceksenetno.contains=" + UPDATED_CEKSENETNO);
    }

    @Test
    @Transactional
    void getAllCekSenetsByCeksenetnoNotContainsSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where ceksenetno does not contain DEFAULT_CEKSENETNO
        defaultCekSenetShouldNotBeFound("ceksenetno.doesNotContain=" + DEFAULT_CEKSENETNO);

        // Get all the cekSenetList where ceksenetno does not contain UPDATED_CEKSENETNO
        defaultCekSenetShouldBeFound("ceksenetno.doesNotContain=" + UPDATED_CEKSENETNO);
    }

    @Test
    @Transactional
    void getAllCekSenetsByEvraktipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where evraktipi equals to DEFAULT_EVRAKTIPI
        defaultCekSenetShouldBeFound("evraktipi.equals=" + DEFAULT_EVRAKTIPI);

        // Get all the cekSenetList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultCekSenetShouldNotBeFound("evraktipi.equals=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByEvraktipiIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where evraktipi in DEFAULT_EVRAKTIPI or UPDATED_EVRAKTIPI
        defaultCekSenetShouldBeFound("evraktipi.in=" + DEFAULT_EVRAKTIPI + "," + UPDATED_EVRAKTIPI);

        // Get all the cekSenetList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultCekSenetShouldNotBeFound("evraktipi.in=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByEvraktipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where evraktipi is not null
        defaultCekSenetShouldBeFound("evraktipi.specified=true");

        // Get all the cekSenetList where evraktipi is null
        defaultCekSenetShouldNotBeFound("evraktipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByDepotipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where depotipi equals to DEFAULT_DEPOTIPI
        defaultCekSenetShouldBeFound("depotipi.equals=" + DEFAULT_DEPOTIPI);

        // Get all the cekSenetList where depotipi equals to UPDATED_DEPOTIPI
        defaultCekSenetShouldNotBeFound("depotipi.equals=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByDepotipiIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where depotipi in DEFAULT_DEPOTIPI or UPDATED_DEPOTIPI
        defaultCekSenetShouldBeFound("depotipi.in=" + DEFAULT_DEPOTIPI + "," + UPDATED_DEPOTIPI);

        // Get all the cekSenetList where depotipi equals to UPDATED_DEPOTIPI
        defaultCekSenetShouldNotBeFound("depotipi.in=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByDepotipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where depotipi is not null
        defaultCekSenetShouldBeFound("depotipi.specified=true");

        // Get all the cekSenetList where depotipi is null
        defaultCekSenetShouldNotBeFound("depotipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi equals to DEFAULT_TARIHI
        defaultCekSenetShouldBeFound("tarihi.equals=" + DEFAULT_TARIHI);

        // Get all the cekSenetList where tarihi equals to UPDATED_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.equals=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi in DEFAULT_TARIHI or UPDATED_TARIHI
        defaultCekSenetShouldBeFound("tarihi.in=" + DEFAULT_TARIHI + "," + UPDATED_TARIHI);

        // Get all the cekSenetList where tarihi equals to UPDATED_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.in=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi is not null
        defaultCekSenetShouldBeFound("tarihi.specified=true");

        // Get all the cekSenetList where tarihi is null
        defaultCekSenetShouldNotBeFound("tarihi.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi is greater than or equal to DEFAULT_TARIHI
        defaultCekSenetShouldBeFound("tarihi.greaterThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the cekSenetList where tarihi is greater than or equal to UPDATED_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.greaterThanOrEqual=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi is less than or equal to DEFAULT_TARIHI
        defaultCekSenetShouldBeFound("tarihi.lessThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the cekSenetList where tarihi is less than or equal to SMALLER_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.lessThanOrEqual=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsLessThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi is less than DEFAULT_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.lessThan=" + DEFAULT_TARIHI);

        // Get all the cekSenetList where tarihi is less than UPDATED_TARIHI
        defaultCekSenetShouldBeFound("tarihi.lessThan=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTarihiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tarihi is greater than DEFAULT_TARIHI
        defaultCekSenetShouldNotBeFound("tarihi.greaterThan=" + DEFAULT_TARIHI);

        // Get all the cekSenetList where tarihi is greater than SMALLER_TARIHI
        defaultCekSenetShouldBeFound("tarihi.greaterThan=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade equals to DEFAULT_VADE
        defaultCekSenetShouldBeFound("vade.equals=" + DEFAULT_VADE);

        // Get all the cekSenetList where vade equals to UPDATED_VADE
        defaultCekSenetShouldNotBeFound("vade.equals=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade in DEFAULT_VADE or UPDATED_VADE
        defaultCekSenetShouldBeFound("vade.in=" + DEFAULT_VADE + "," + UPDATED_VADE);

        // Get all the cekSenetList where vade equals to UPDATED_VADE
        defaultCekSenetShouldNotBeFound("vade.in=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade is not null
        defaultCekSenetShouldBeFound("vade.specified=true");

        // Get all the cekSenetList where vade is null
        defaultCekSenetShouldNotBeFound("vade.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade is greater than or equal to DEFAULT_VADE
        defaultCekSenetShouldBeFound("vade.greaterThanOrEqual=" + DEFAULT_VADE);

        // Get all the cekSenetList where vade is greater than or equal to UPDATED_VADE
        defaultCekSenetShouldNotBeFound("vade.greaterThanOrEqual=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade is less than or equal to DEFAULT_VADE
        defaultCekSenetShouldBeFound("vade.lessThanOrEqual=" + DEFAULT_VADE);

        // Get all the cekSenetList where vade is less than or equal to SMALLER_VADE
        defaultCekSenetShouldNotBeFound("vade.lessThanOrEqual=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsLessThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade is less than DEFAULT_VADE
        defaultCekSenetShouldNotBeFound("vade.lessThan=" + DEFAULT_VADE);

        // Get all the cekSenetList where vade is less than UPDATED_VADE
        defaultCekSenetShouldBeFound("vade.lessThan=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByVadeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where vade is greater than DEFAULT_VADE
        defaultCekSenetShouldNotBeFound("vade.greaterThan=" + DEFAULT_VADE);

        // Get all the cekSenetList where vade is greater than SMALLER_VADE
        defaultCekSenetShouldBeFound("vade.greaterThan=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllCekSenetsByAciklamaIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where aciklama equals to DEFAULT_ACIKLAMA
        defaultCekSenetShouldBeFound("aciklama.equals=" + DEFAULT_ACIKLAMA);

        // Get all the cekSenetList where aciklama equals to UPDATED_ACIKLAMA
        defaultCekSenetShouldNotBeFound("aciklama.equals=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCekSenetsByAciklamaIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where aciklama in DEFAULT_ACIKLAMA or UPDATED_ACIKLAMA
        defaultCekSenetShouldBeFound("aciklama.in=" + DEFAULT_ACIKLAMA + "," + UPDATED_ACIKLAMA);

        // Get all the cekSenetList where aciklama equals to UPDATED_ACIKLAMA
        defaultCekSenetShouldNotBeFound("aciklama.in=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCekSenetsByAciklamaIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where aciklama is not null
        defaultCekSenetShouldBeFound("aciklama.specified=true");

        // Get all the cekSenetList where aciklama is null
        defaultCekSenetShouldNotBeFound("aciklama.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByAciklamaContainsSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where aciklama contains DEFAULT_ACIKLAMA
        defaultCekSenetShouldBeFound("aciklama.contains=" + DEFAULT_ACIKLAMA);

        // Get all the cekSenetList where aciklama contains UPDATED_ACIKLAMA
        defaultCekSenetShouldNotBeFound("aciklama.contains=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCekSenetsByAciklamaNotContainsSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where aciklama does not contain DEFAULT_ACIKLAMA
        defaultCekSenetShouldNotBeFound("aciklama.doesNotContain=" + DEFAULT_ACIKLAMA);

        // Get all the cekSenetList where aciklama does not contain UPDATED_ACIKLAMA
        defaultCekSenetShouldBeFound("aciklama.doesNotContain=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar equals to DEFAULT_TUTAR
        defaultCekSenetShouldBeFound("tutar.equals=" + DEFAULT_TUTAR);

        // Get all the cekSenetList where tutar equals to UPDATED_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.equals=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsInShouldWork() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar in DEFAULT_TUTAR or UPDATED_TUTAR
        defaultCekSenetShouldBeFound("tutar.in=" + DEFAULT_TUTAR + "," + UPDATED_TUTAR);

        // Get all the cekSenetList where tutar equals to UPDATED_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.in=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsNullOrNotNull() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar is not null
        defaultCekSenetShouldBeFound("tutar.specified=true");

        // Get all the cekSenetList where tutar is null
        defaultCekSenetShouldNotBeFound("tutar.specified=false");
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar is greater than or equal to DEFAULT_TUTAR
        defaultCekSenetShouldBeFound("tutar.greaterThanOrEqual=" + DEFAULT_TUTAR);

        // Get all the cekSenetList where tutar is greater than or equal to UPDATED_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.greaterThanOrEqual=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar is less than or equal to DEFAULT_TUTAR
        defaultCekSenetShouldBeFound("tutar.lessThanOrEqual=" + DEFAULT_TUTAR);

        // Get all the cekSenetList where tutar is less than or equal to SMALLER_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.lessThanOrEqual=" + SMALLER_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsLessThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar is less than DEFAULT_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.lessThan=" + DEFAULT_TUTAR);

        // Get all the cekSenetList where tutar is less than UPDATED_TUTAR
        defaultCekSenetShouldBeFound("tutar.lessThan=" + UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByTutarIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        // Get all the cekSenetList where tutar is greater than DEFAULT_TUTAR
        defaultCekSenetShouldNotBeFound("tutar.greaterThan=" + DEFAULT_TUTAR);

        // Get all the cekSenetList where tutar is greater than SMALLER_TUTAR
        defaultCekSenetShouldBeFound("tutar.greaterThan=" + SMALLER_TUTAR);
    }

    @Test
    @Transactional
    void getAllCekSenetsByCekSenetDetaysIsEqualToSomething() throws Exception {
        CekSenetDetay cekSenetDetays;
        if (TestUtil.findAll(em, CekSenetDetay.class).isEmpty()) {
            cekSenetRepository.saveAndFlush(cekSenet);
            cekSenetDetays = CekSenetDetayResourceIT.createEntity(em);
        } else {
            cekSenetDetays = TestUtil.findAll(em, CekSenetDetay.class).get(0);
        }
        em.persist(cekSenetDetays);
        em.flush();
        cekSenet.addCekSenetDetays(cekSenetDetays);
        cekSenetRepository.saveAndFlush(cekSenet);
        Long cekSenetDetaysId = cekSenetDetays.getId();

        // Get all the cekSenetList where cekSenetDetays equals to cekSenetDetaysId
        defaultCekSenetShouldBeFound("cekSenetDetaysId.equals=" + cekSenetDetaysId);

        // Get all the cekSenetList where cekSenetDetays equals to (cekSenetDetaysId + 1)
        defaultCekSenetShouldNotBeFound("cekSenetDetaysId.equals=" + (cekSenetDetaysId + 1));
    }

    @Test
    @Transactional
    void getAllCekSenetsBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            cekSenetRepository.saveAndFlush(cekSenet);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        cekSenet.setSicil(sicil);
        cekSenetRepository.saveAndFlush(cekSenet);
        Long sicilId = sicil.getId();

        // Get all the cekSenetList where sicil equals to sicilId
        defaultCekSenetShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the cekSenetList where sicil equals to (sicilId + 1)
        defaultCekSenetShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCekSenetShouldBeFound(String filter) throws Exception {
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cekSenet.getId().intValue())))
            .andExpect(jsonPath("$.[*].ceksenetno").value(hasItem(DEFAULT_CEKSENETNO)))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].tutar").value(hasItem(sameNumber(DEFAULT_TUTAR))));

        // Check, that the count call also returns 1
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCekSenetShouldNotBeFound(String filter) throws Exception {
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCekSenetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCekSenet() throws Exception {
        // Get the cekSenet
        restCekSenetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCekSenet() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();

        // Update the cekSenet
        CekSenet updatedCekSenet = cekSenetRepository.findById(cekSenet.getId()).get();
        // Disconnect from session so that the updates on updatedCekSenet are not directly saved in db
        em.detach(updatedCekSenet);
        updatedCekSenet
            .ceksenetno(UPDATED_CEKSENETNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutar(UPDATED_TUTAR);
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(updatedCekSenet);

        restCekSenetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cekSenetDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isOk());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
        CekSenet testCekSenet = cekSenetList.get(cekSenetList.size() - 1);
        assertThat(testCekSenet.getCeksenetno()).isEqualTo(UPDATED_CEKSENETNO);
        assertThat(testCekSenet.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testCekSenet.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCekSenet.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenet.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCekSenet.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testCekSenet.getTutar()).isEqualByComparingTo(UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void putNonExistingCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cekSenetDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cekSenetDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCekSenetWithPatch() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();

        // Update the cekSenet using partial update
        CekSenet partialUpdatedCekSenet = new CekSenet();
        partialUpdatedCekSenet.setId(cekSenet.getId());

        partialUpdatedCekSenet.ceksenetno(UPDATED_CEKSENETNO).depotipi(UPDATED_DEPOTIPI).tarihi(UPDATED_TARIHI).vade(UPDATED_VADE);

        restCekSenetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCekSenet.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCekSenet))
            )
            .andExpect(status().isOk());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
        CekSenet testCekSenet = cekSenetList.get(cekSenetList.size() - 1);
        assertThat(testCekSenet.getCeksenetno()).isEqualTo(UPDATED_CEKSENETNO);
        assertThat(testCekSenet.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testCekSenet.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCekSenet.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenet.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCekSenet.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testCekSenet.getTutar()).isEqualByComparingTo(DEFAULT_TUTAR);
    }

    @Test
    @Transactional
    void fullUpdateCekSenetWithPatch() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();

        // Update the cekSenet using partial update
        CekSenet partialUpdatedCekSenet = new CekSenet();
        partialUpdatedCekSenet.setId(cekSenet.getId());

        partialUpdatedCekSenet
            .ceksenetno(UPDATED_CEKSENETNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutar(UPDATED_TUTAR);

        restCekSenetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCekSenet.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCekSenet))
            )
            .andExpect(status().isOk());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
        CekSenet testCekSenet = cekSenetList.get(cekSenetList.size() - 1);
        assertThat(testCekSenet.getCeksenetno()).isEqualTo(UPDATED_CEKSENETNO);
        assertThat(testCekSenet.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testCekSenet.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCekSenet.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testCekSenet.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCekSenet.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testCekSenet.getTutar()).isEqualByComparingTo(UPDATED_TUTAR);
    }

    @Test
    @Transactional
    void patchNonExistingCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cekSenetDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCekSenet() throws Exception {
        int databaseSizeBeforeUpdate = cekSenetRepository.findAll().size();
        cekSenet.setId(count.incrementAndGet());

        // Create the CekSenet
        CekSenetDTO cekSenetDTO = cekSenetMapper.toDto(cekSenet);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCekSenetMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cekSenetDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CekSenet in the database
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCekSenet() throws Exception {
        // Initialize the database
        cekSenetRepository.saveAndFlush(cekSenet);

        int databaseSizeBeforeDelete = cekSenetRepository.findAll().size();

        // Delete the cekSenet
        restCekSenetMockMvc
            .perform(delete(ENTITY_API_URL_ID, cekSenet.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CekSenet> cekSenetList = cekSenetRepository.findAll();
        assertThat(cekSenetList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
