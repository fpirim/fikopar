package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.Makbuz;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.MakbuzRepository;
import com.dizel.fikopar.service.criteria.MakbuzCriteria;
import com.dizel.fikopar.service.dto.MakbuzDTO;
import com.dizel.fikopar.service.mapper.MakbuzMapper;
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
 * Integration tests for the {@link MakbuzResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MakbuzResourceIT {

    private static final String DEFAULT_MAKBUZNO = "AAAAAAAAAA";
    private static final String UPDATED_MAKBUZNO = "BBBBBBBBBB";

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

    private static final BigDecimal DEFAULT_TUTARI = new BigDecimal(1);
    private static final BigDecimal UPDATED_TUTARI = new BigDecimal(2);
    private static final BigDecimal SMALLER_TUTARI = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/makbuzs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MakbuzRepository makbuzRepository;

    @Autowired
    private MakbuzMapper makbuzMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMakbuzMockMvc;

    private Makbuz makbuz;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Makbuz createEntity(EntityManager em) {
        Makbuz makbuz = new Makbuz()
            .makbuzno(DEFAULT_MAKBUZNO)
            .evraktipi(DEFAULT_EVRAKTIPI)
            .depotipi(DEFAULT_DEPOTIPI)
            .tarihi(DEFAULT_TARIHI)
            .vade(DEFAULT_VADE)
            .aciklama(DEFAULT_ACIKLAMA)
            .tutari(DEFAULT_TUTARI);
        return makbuz;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Makbuz createUpdatedEntity(EntityManager em) {
        Makbuz makbuz = new Makbuz()
            .makbuzno(UPDATED_MAKBUZNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutari(UPDATED_TUTARI);
        return makbuz;
    }

    @BeforeEach
    public void initTest() {
        makbuz = createEntity(em);
    }

    @Test
    @Transactional
    void createMakbuz() throws Exception {
        int databaseSizeBeforeCreate = makbuzRepository.findAll().size();
        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);
        restMakbuzMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(makbuzDTO)))
            .andExpect(status().isCreated());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeCreate + 1);
        Makbuz testMakbuz = makbuzList.get(makbuzList.size() - 1);
        assertThat(testMakbuz.getMakbuzno()).isEqualTo(DEFAULT_MAKBUZNO);
        assertThat(testMakbuz.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testMakbuz.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testMakbuz.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testMakbuz.getVade()).isEqualTo(DEFAULT_VADE);
        assertThat(testMakbuz.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testMakbuz.getTutari()).isEqualByComparingTo(DEFAULT_TUTARI);
    }

    @Test
    @Transactional
    void createMakbuzWithExistingId() throws Exception {
        // Create the Makbuz with an existing ID
        makbuz.setId(1L);
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        int databaseSizeBeforeCreate = makbuzRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMakbuzMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(makbuzDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllMakbuzs() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(makbuz.getId().intValue())))
            .andExpect(jsonPath("$.[*].makbuzno").value(hasItem(DEFAULT_MAKBUZNO)))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].tutari").value(hasItem(sameNumber(DEFAULT_TUTARI))));
    }

    @Test
    @Transactional
    void getMakbuz() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get the makbuz
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL_ID, makbuz.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(makbuz.getId().intValue()))
            .andExpect(jsonPath("$.makbuzno").value(DEFAULT_MAKBUZNO))
            .andExpect(jsonPath("$.evraktipi").value(DEFAULT_EVRAKTIPI.toString()))
            .andExpect(jsonPath("$.depotipi").value(DEFAULT_DEPOTIPI.toString()))
            .andExpect(jsonPath("$.tarihi").value(DEFAULT_TARIHI.toString()))
            .andExpect(jsonPath("$.vade").value(DEFAULT_VADE.toString()))
            .andExpect(jsonPath("$.aciklama").value(DEFAULT_ACIKLAMA))
            .andExpect(jsonPath("$.tutari").value(sameNumber(DEFAULT_TUTARI)));
    }

    @Test
    @Transactional
    void getMakbuzsByIdFiltering() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        Long id = makbuz.getId();

        defaultMakbuzShouldBeFound("id.equals=" + id);
        defaultMakbuzShouldNotBeFound("id.notEquals=" + id);

        defaultMakbuzShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultMakbuzShouldNotBeFound("id.greaterThan=" + id);

        defaultMakbuzShouldBeFound("id.lessThanOrEqual=" + id);
        defaultMakbuzShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMakbuzsByMakbuznoIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where makbuzno equals to DEFAULT_MAKBUZNO
        defaultMakbuzShouldBeFound("makbuzno.equals=" + DEFAULT_MAKBUZNO);

        // Get all the makbuzList where makbuzno equals to UPDATED_MAKBUZNO
        defaultMakbuzShouldNotBeFound("makbuzno.equals=" + UPDATED_MAKBUZNO);
    }

    @Test
    @Transactional
    void getAllMakbuzsByMakbuznoIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where makbuzno in DEFAULT_MAKBUZNO or UPDATED_MAKBUZNO
        defaultMakbuzShouldBeFound("makbuzno.in=" + DEFAULT_MAKBUZNO + "," + UPDATED_MAKBUZNO);

        // Get all the makbuzList where makbuzno equals to UPDATED_MAKBUZNO
        defaultMakbuzShouldNotBeFound("makbuzno.in=" + UPDATED_MAKBUZNO);
    }

    @Test
    @Transactional
    void getAllMakbuzsByMakbuznoIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where makbuzno is not null
        defaultMakbuzShouldBeFound("makbuzno.specified=true");

        // Get all the makbuzList where makbuzno is null
        defaultMakbuzShouldNotBeFound("makbuzno.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByMakbuznoContainsSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where makbuzno contains DEFAULT_MAKBUZNO
        defaultMakbuzShouldBeFound("makbuzno.contains=" + DEFAULT_MAKBUZNO);

        // Get all the makbuzList where makbuzno contains UPDATED_MAKBUZNO
        defaultMakbuzShouldNotBeFound("makbuzno.contains=" + UPDATED_MAKBUZNO);
    }

    @Test
    @Transactional
    void getAllMakbuzsByMakbuznoNotContainsSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where makbuzno does not contain DEFAULT_MAKBUZNO
        defaultMakbuzShouldNotBeFound("makbuzno.doesNotContain=" + DEFAULT_MAKBUZNO);

        // Get all the makbuzList where makbuzno does not contain UPDATED_MAKBUZNO
        defaultMakbuzShouldBeFound("makbuzno.doesNotContain=" + UPDATED_MAKBUZNO);
    }

    @Test
    @Transactional
    void getAllMakbuzsByEvraktipiIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where evraktipi equals to DEFAULT_EVRAKTIPI
        defaultMakbuzShouldBeFound("evraktipi.equals=" + DEFAULT_EVRAKTIPI);

        // Get all the makbuzList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultMakbuzShouldNotBeFound("evraktipi.equals=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByEvraktipiIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where evraktipi in DEFAULT_EVRAKTIPI or UPDATED_EVRAKTIPI
        defaultMakbuzShouldBeFound("evraktipi.in=" + DEFAULT_EVRAKTIPI + "," + UPDATED_EVRAKTIPI);

        // Get all the makbuzList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultMakbuzShouldNotBeFound("evraktipi.in=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByEvraktipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where evraktipi is not null
        defaultMakbuzShouldBeFound("evraktipi.specified=true");

        // Get all the makbuzList where evraktipi is null
        defaultMakbuzShouldNotBeFound("evraktipi.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByDepotipiIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where depotipi equals to DEFAULT_DEPOTIPI
        defaultMakbuzShouldBeFound("depotipi.equals=" + DEFAULT_DEPOTIPI);

        // Get all the makbuzList where depotipi equals to UPDATED_DEPOTIPI
        defaultMakbuzShouldNotBeFound("depotipi.equals=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByDepotipiIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where depotipi in DEFAULT_DEPOTIPI or UPDATED_DEPOTIPI
        defaultMakbuzShouldBeFound("depotipi.in=" + DEFAULT_DEPOTIPI + "," + UPDATED_DEPOTIPI);

        // Get all the makbuzList where depotipi equals to UPDATED_DEPOTIPI
        defaultMakbuzShouldNotBeFound("depotipi.in=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByDepotipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where depotipi is not null
        defaultMakbuzShouldBeFound("depotipi.specified=true");

        // Get all the makbuzList where depotipi is null
        defaultMakbuzShouldNotBeFound("depotipi.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi equals to DEFAULT_TARIHI
        defaultMakbuzShouldBeFound("tarihi.equals=" + DEFAULT_TARIHI);

        // Get all the makbuzList where tarihi equals to UPDATED_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.equals=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi in DEFAULT_TARIHI or UPDATED_TARIHI
        defaultMakbuzShouldBeFound("tarihi.in=" + DEFAULT_TARIHI + "," + UPDATED_TARIHI);

        // Get all the makbuzList where tarihi equals to UPDATED_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.in=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi is not null
        defaultMakbuzShouldBeFound("tarihi.specified=true");

        // Get all the makbuzList where tarihi is null
        defaultMakbuzShouldNotBeFound("tarihi.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi is greater than or equal to DEFAULT_TARIHI
        defaultMakbuzShouldBeFound("tarihi.greaterThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the makbuzList where tarihi is greater than or equal to UPDATED_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.greaterThanOrEqual=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi is less than or equal to DEFAULT_TARIHI
        defaultMakbuzShouldBeFound("tarihi.lessThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the makbuzList where tarihi is less than or equal to SMALLER_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.lessThanOrEqual=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsLessThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi is less than DEFAULT_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.lessThan=" + DEFAULT_TARIHI);

        // Get all the makbuzList where tarihi is less than UPDATED_TARIHI
        defaultMakbuzShouldBeFound("tarihi.lessThan=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTarihiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tarihi is greater than DEFAULT_TARIHI
        defaultMakbuzShouldNotBeFound("tarihi.greaterThan=" + DEFAULT_TARIHI);

        // Get all the makbuzList where tarihi is greater than SMALLER_TARIHI
        defaultMakbuzShouldBeFound("tarihi.greaterThan=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade equals to DEFAULT_VADE
        defaultMakbuzShouldBeFound("vade.equals=" + DEFAULT_VADE);

        // Get all the makbuzList where vade equals to UPDATED_VADE
        defaultMakbuzShouldNotBeFound("vade.equals=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade in DEFAULT_VADE or UPDATED_VADE
        defaultMakbuzShouldBeFound("vade.in=" + DEFAULT_VADE + "," + UPDATED_VADE);

        // Get all the makbuzList where vade equals to UPDATED_VADE
        defaultMakbuzShouldNotBeFound("vade.in=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade is not null
        defaultMakbuzShouldBeFound("vade.specified=true");

        // Get all the makbuzList where vade is null
        defaultMakbuzShouldNotBeFound("vade.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade is greater than or equal to DEFAULT_VADE
        defaultMakbuzShouldBeFound("vade.greaterThanOrEqual=" + DEFAULT_VADE);

        // Get all the makbuzList where vade is greater than or equal to UPDATED_VADE
        defaultMakbuzShouldNotBeFound("vade.greaterThanOrEqual=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade is less than or equal to DEFAULT_VADE
        defaultMakbuzShouldBeFound("vade.lessThanOrEqual=" + DEFAULT_VADE);

        // Get all the makbuzList where vade is less than or equal to SMALLER_VADE
        defaultMakbuzShouldNotBeFound("vade.lessThanOrEqual=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsLessThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade is less than DEFAULT_VADE
        defaultMakbuzShouldNotBeFound("vade.lessThan=" + DEFAULT_VADE);

        // Get all the makbuzList where vade is less than UPDATED_VADE
        defaultMakbuzShouldBeFound("vade.lessThan=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByVadeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where vade is greater than DEFAULT_VADE
        defaultMakbuzShouldNotBeFound("vade.greaterThan=" + DEFAULT_VADE);

        // Get all the makbuzList where vade is greater than SMALLER_VADE
        defaultMakbuzShouldBeFound("vade.greaterThan=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllMakbuzsByAciklamaIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where aciklama equals to DEFAULT_ACIKLAMA
        defaultMakbuzShouldBeFound("aciklama.equals=" + DEFAULT_ACIKLAMA);

        // Get all the makbuzList where aciklama equals to UPDATED_ACIKLAMA
        defaultMakbuzShouldNotBeFound("aciklama.equals=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllMakbuzsByAciklamaIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where aciklama in DEFAULT_ACIKLAMA or UPDATED_ACIKLAMA
        defaultMakbuzShouldBeFound("aciklama.in=" + DEFAULT_ACIKLAMA + "," + UPDATED_ACIKLAMA);

        // Get all the makbuzList where aciklama equals to UPDATED_ACIKLAMA
        defaultMakbuzShouldNotBeFound("aciklama.in=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllMakbuzsByAciklamaIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where aciklama is not null
        defaultMakbuzShouldBeFound("aciklama.specified=true");

        // Get all the makbuzList where aciklama is null
        defaultMakbuzShouldNotBeFound("aciklama.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByAciklamaContainsSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where aciklama contains DEFAULT_ACIKLAMA
        defaultMakbuzShouldBeFound("aciklama.contains=" + DEFAULT_ACIKLAMA);

        // Get all the makbuzList where aciklama contains UPDATED_ACIKLAMA
        defaultMakbuzShouldNotBeFound("aciklama.contains=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllMakbuzsByAciklamaNotContainsSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where aciklama does not contain DEFAULT_ACIKLAMA
        defaultMakbuzShouldNotBeFound("aciklama.doesNotContain=" + DEFAULT_ACIKLAMA);

        // Get all the makbuzList where aciklama does not contain UPDATED_ACIKLAMA
        defaultMakbuzShouldBeFound("aciklama.doesNotContain=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari equals to DEFAULT_TUTARI
        defaultMakbuzShouldBeFound("tutari.equals=" + DEFAULT_TUTARI);

        // Get all the makbuzList where tutari equals to UPDATED_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.equals=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsInShouldWork() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari in DEFAULT_TUTARI or UPDATED_TUTARI
        defaultMakbuzShouldBeFound("tutari.in=" + DEFAULT_TUTARI + "," + UPDATED_TUTARI);

        // Get all the makbuzList where tutari equals to UPDATED_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.in=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsNullOrNotNull() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari is not null
        defaultMakbuzShouldBeFound("tutari.specified=true");

        // Get all the makbuzList where tutari is null
        defaultMakbuzShouldNotBeFound("tutari.specified=false");
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari is greater than or equal to DEFAULT_TUTARI
        defaultMakbuzShouldBeFound("tutari.greaterThanOrEqual=" + DEFAULT_TUTARI);

        // Get all the makbuzList where tutari is greater than or equal to UPDATED_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.greaterThanOrEqual=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari is less than or equal to DEFAULT_TUTARI
        defaultMakbuzShouldBeFound("tutari.lessThanOrEqual=" + DEFAULT_TUTARI);

        // Get all the makbuzList where tutari is less than or equal to SMALLER_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.lessThanOrEqual=" + SMALLER_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsLessThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari is less than DEFAULT_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.lessThan=" + DEFAULT_TUTARI);

        // Get all the makbuzList where tutari is less than UPDATED_TUTARI
        defaultMakbuzShouldBeFound("tutari.lessThan=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByTutariIsGreaterThanSomething() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        // Get all the makbuzList where tutari is greater than DEFAULT_TUTARI
        defaultMakbuzShouldNotBeFound("tutari.greaterThan=" + DEFAULT_TUTARI);

        // Get all the makbuzList where tutari is greater than SMALLER_TUTARI
        defaultMakbuzShouldBeFound("tutari.greaterThan=" + SMALLER_TUTARI);
    }

    @Test
    @Transactional
    void getAllMakbuzsByCariIsEqualToSomething() throws Exception {
        Cari cari;
        if (TestUtil.findAll(em, Cari.class).isEmpty()) {
            makbuzRepository.saveAndFlush(makbuz);
            cari = CariResourceIT.createEntity(em);
        } else {
            cari = TestUtil.findAll(em, Cari.class).get(0);
        }
        em.persist(cari);
        em.flush();
        makbuz.setCari(cari);
        makbuzRepository.saveAndFlush(makbuz);
        Long cariId = cari.getId();

        // Get all the makbuzList where cari equals to cariId
        defaultMakbuzShouldBeFound("cariId.equals=" + cariId);

        // Get all the makbuzList where cari equals to (cariId + 1)
        defaultMakbuzShouldNotBeFound("cariId.equals=" + (cariId + 1));
    }

    @Test
    @Transactional
    void getAllMakbuzsBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            makbuzRepository.saveAndFlush(makbuz);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        makbuz.setSicil(sicil);
        makbuzRepository.saveAndFlush(makbuz);
        Long sicilId = sicil.getId();

        // Get all the makbuzList where sicil equals to sicilId
        defaultMakbuzShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the makbuzList where sicil equals to (sicilId + 1)
        defaultMakbuzShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMakbuzShouldBeFound(String filter) throws Exception {
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(makbuz.getId().intValue())))
            .andExpect(jsonPath("$.[*].makbuzno").value(hasItem(DEFAULT_MAKBUZNO)))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].tutari").value(hasItem(sameNumber(DEFAULT_TUTARI))));

        // Check, that the count call also returns 1
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMakbuzShouldNotBeFound(String filter) throws Exception {
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMakbuzMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMakbuz() throws Exception {
        // Get the makbuz
        restMakbuzMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMakbuz() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();

        // Update the makbuz
        Makbuz updatedMakbuz = makbuzRepository.findById(makbuz.getId()).get();
        // Disconnect from session so that the updates on updatedMakbuz are not directly saved in db
        em.detach(updatedMakbuz);
        updatedMakbuz
            .makbuzno(UPDATED_MAKBUZNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutari(UPDATED_TUTARI);
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(updatedMakbuz);

        restMakbuzMockMvc
            .perform(
                put(ENTITY_API_URL_ID, makbuzDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isOk());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
        Makbuz testMakbuz = makbuzList.get(makbuzList.size() - 1);
        assertThat(testMakbuz.getMakbuzno()).isEqualTo(UPDATED_MAKBUZNO);
        assertThat(testMakbuz.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testMakbuz.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testMakbuz.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testMakbuz.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testMakbuz.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testMakbuz.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void putNonExistingMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(
                put(ENTITY_API_URL_ID, makbuzDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(makbuzDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMakbuzWithPatch() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();

        // Update the makbuz using partial update
        Makbuz partialUpdatedMakbuz = new Makbuz();
        partialUpdatedMakbuz.setId(makbuz.getId());

        partialUpdatedMakbuz.makbuzno(UPDATED_MAKBUZNO).tutari(UPDATED_TUTARI);

        restMakbuzMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMakbuz.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMakbuz))
            )
            .andExpect(status().isOk());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
        Makbuz testMakbuz = makbuzList.get(makbuzList.size() - 1);
        assertThat(testMakbuz.getMakbuzno()).isEqualTo(UPDATED_MAKBUZNO);
        assertThat(testMakbuz.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testMakbuz.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testMakbuz.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testMakbuz.getVade()).isEqualTo(DEFAULT_VADE);
        assertThat(testMakbuz.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testMakbuz.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void fullUpdateMakbuzWithPatch() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();

        // Update the makbuz using partial update
        Makbuz partialUpdatedMakbuz = new Makbuz();
        partialUpdatedMakbuz.setId(makbuz.getId());

        partialUpdatedMakbuz
            .makbuzno(UPDATED_MAKBUZNO)
            .evraktipi(UPDATED_EVRAKTIPI)
            .depotipi(UPDATED_DEPOTIPI)
            .tarihi(UPDATED_TARIHI)
            .vade(UPDATED_VADE)
            .aciklama(UPDATED_ACIKLAMA)
            .tutari(UPDATED_TUTARI);

        restMakbuzMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMakbuz.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMakbuz))
            )
            .andExpect(status().isOk());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
        Makbuz testMakbuz = makbuzList.get(makbuzList.size() - 1);
        assertThat(testMakbuz.getMakbuzno()).isEqualTo(UPDATED_MAKBUZNO);
        assertThat(testMakbuz.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testMakbuz.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testMakbuz.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testMakbuz.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testMakbuz.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testMakbuz.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void patchNonExistingMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, makbuzDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMakbuz() throws Exception {
        int databaseSizeBeforeUpdate = makbuzRepository.findAll().size();
        makbuz.setId(count.incrementAndGet());

        // Create the Makbuz
        MakbuzDTO makbuzDTO = makbuzMapper.toDto(makbuz);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMakbuzMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(makbuzDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Makbuz in the database
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMakbuz() throws Exception {
        // Initialize the database
        makbuzRepository.saveAndFlush(makbuz);

        int databaseSizeBeforeDelete = makbuzRepository.findAll().size();

        // Delete the makbuz
        restMakbuzMockMvc
            .perform(delete(ENTITY_API_URL_ID, makbuz.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Makbuz> makbuzList = makbuzRepository.findAll();
        assertThat(makbuzList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
