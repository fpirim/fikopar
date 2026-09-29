package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.Cari;
import com.dizel.fikopar.domain.Sicil;
import com.dizel.fikopar.domain.enumeration.DepoTipi;
import com.dizel.fikopar.domain.enumeration.EvrakTipi;
import com.dizel.fikopar.repository.CariRepository;
import com.dizel.fikopar.service.criteria.CariCriteria;
import com.dizel.fikopar.service.dto.CariDTO;
import com.dizel.fikopar.service.mapper.CariMapper;
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
 * Integration tests for the {@link CariResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CariResourceIT {

    private static final EvrakTipi DEFAULT_TIPI = EvrakTipi.IRSALIYELI_SATIS_FATURASI;
    private static final EvrakTipi UPDATED_TIPI = EvrakTipi.SATIS_FATURASI;

    private static final String DEFAULT_EVRAKNO = "AAAAAAAAAA";
    private static final String UPDATED_EVRAKNO = "BBBBBBBBBB";

    private static final DepoTipi DEFAULT_DEPOTIPI = DepoTipi.DEPO1;
    private static final DepoTipi UPDATED_DEPOTIPI = DepoTipi.DEPO2;

    private static final LocalDate DEFAULT_TARIH = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TARIH = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TARIH = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_VADE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VADE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_VADE = LocalDate.ofEpochDay(-1L);

    private static final Integer DEFAULT_GUN = 1;
    private static final Integer UPDATED_GUN = 2;
    private static final Integer SMALLER_GUN = 1 - 1;

    private static final String DEFAULT_ACIKLAMA = "AAAAAAAAAA";
    private static final String UPDATED_ACIKLAMA = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_BORC = new BigDecimal(1);
    private static final BigDecimal UPDATED_BORC = new BigDecimal(2);
    private static final BigDecimal SMALLER_BORC = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_ALACAK = new BigDecimal(1);
    private static final BigDecimal UPDATED_ALACAK = new BigDecimal(2);
    private static final BigDecimal SMALLER_ALACAK = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/caris";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CariRepository cariRepository;

    @Autowired
    private CariMapper cariMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCariMockMvc;

    private Cari cari;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cari createEntity(EntityManager em) {
        Cari cari = new Cari()
            .tipi(DEFAULT_TIPI)
            .evrakno(DEFAULT_EVRAKNO)
            .depotipi(DEFAULT_DEPOTIPI)
            .tarih(DEFAULT_TARIH)
            .vade(DEFAULT_VADE)
            .gun(DEFAULT_GUN)
            .aciklama(DEFAULT_ACIKLAMA)
            .borc(DEFAULT_BORC)
            .alacak(DEFAULT_ALACAK);
        return cari;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cari createUpdatedEntity(EntityManager em) {
        Cari cari = new Cari()
            .tipi(UPDATED_TIPI)
            .evrakno(UPDATED_EVRAKNO)
            .depotipi(UPDATED_DEPOTIPI)
            .tarih(UPDATED_TARIH)
            .vade(UPDATED_VADE)
            .gun(UPDATED_GUN)
            .aciklama(UPDATED_ACIKLAMA)
            .borc(UPDATED_BORC)
            .alacak(UPDATED_ALACAK);
        return cari;
    }

    @BeforeEach
    public void initTest() {
        cari = createEntity(em);
    }

    @Test
    @Transactional
    void createCari() throws Exception {
        int databaseSizeBeforeCreate = cariRepository.findAll().size();
        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);
        restCariMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDTO)))
            .andExpect(status().isCreated());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeCreate + 1);
        Cari testCari = cariList.get(cariList.size() - 1);
        assertThat(testCari.getTipi()).isEqualTo(DEFAULT_TIPI);
        assertThat(testCari.getEvrakno()).isEqualTo(DEFAULT_EVRAKNO);
        assertThat(testCari.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testCari.getTarih()).isEqualTo(DEFAULT_TARIH);
        assertThat(testCari.getVade()).isEqualTo(DEFAULT_VADE);
        assertThat(testCari.getGun()).isEqualTo(DEFAULT_GUN);
        assertThat(testCari.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testCari.getBorc()).isEqualByComparingTo(DEFAULT_BORC);
        assertThat(testCari.getAlacak()).isEqualByComparingTo(DEFAULT_ALACAK);
    }

    @Test
    @Transactional
    void createCariWithExistingId() throws Exception {
        // Create the Cari with an existing ID
        cari.setId(1L);
        CariDTO cariDTO = cariMapper.toDto(cari);

        int databaseSizeBeforeCreate = cariRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCariMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCaris() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList
        restCariMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cari.getId().intValue())))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI.toString())))
            .andExpect(jsonPath("$.[*].evrakno").value(hasItem(DEFAULT_EVRAKNO)))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarih").value(hasItem(DEFAULT_TARIH.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].borc").value(hasItem(sameNumber(DEFAULT_BORC))))
            .andExpect(jsonPath("$.[*].alacak").value(hasItem(sameNumber(DEFAULT_ALACAK))));
    }

    @Test
    @Transactional
    void getCari() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get the cari
        restCariMockMvc
            .perform(get(ENTITY_API_URL_ID, cari.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cari.getId().intValue()))
            .andExpect(jsonPath("$.tipi").value(DEFAULT_TIPI.toString()))
            .andExpect(jsonPath("$.evrakno").value(DEFAULT_EVRAKNO))
            .andExpect(jsonPath("$.depotipi").value(DEFAULT_DEPOTIPI.toString()))
            .andExpect(jsonPath("$.tarih").value(DEFAULT_TARIH.toString()))
            .andExpect(jsonPath("$.vade").value(DEFAULT_VADE.toString()))
            .andExpect(jsonPath("$.gun").value(DEFAULT_GUN))
            .andExpect(jsonPath("$.aciklama").value(DEFAULT_ACIKLAMA))
            .andExpect(jsonPath("$.borc").value(sameNumber(DEFAULT_BORC)))
            .andExpect(jsonPath("$.alacak").value(sameNumber(DEFAULT_ALACAK)));
    }

    @Test
    @Transactional
    void getCarisByIdFiltering() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        Long id = cari.getId();

        defaultCariShouldBeFound("id.equals=" + id);
        defaultCariShouldNotBeFound("id.notEquals=" + id);

        defaultCariShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCariShouldNotBeFound("id.greaterThan=" + id);

        defaultCariShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCariShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCarisByTipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tipi equals to DEFAULT_TIPI
        defaultCariShouldBeFound("tipi.equals=" + DEFAULT_TIPI);

        // Get all the cariList where tipi equals to UPDATED_TIPI
        defaultCariShouldNotBeFound("tipi.equals=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllCarisByTipiIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tipi in DEFAULT_TIPI or UPDATED_TIPI
        defaultCariShouldBeFound("tipi.in=" + DEFAULT_TIPI + "," + UPDATED_TIPI);

        // Get all the cariList where tipi equals to UPDATED_TIPI
        defaultCariShouldNotBeFound("tipi.in=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllCarisByTipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tipi is not null
        defaultCariShouldBeFound("tipi.specified=true");

        // Get all the cariList where tipi is null
        defaultCariShouldNotBeFound("tipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByEvraknoIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where evrakno equals to DEFAULT_EVRAKNO
        defaultCariShouldBeFound("evrakno.equals=" + DEFAULT_EVRAKNO);

        // Get all the cariList where evrakno equals to UPDATED_EVRAKNO
        defaultCariShouldNotBeFound("evrakno.equals=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllCarisByEvraknoIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where evrakno in DEFAULT_EVRAKNO or UPDATED_EVRAKNO
        defaultCariShouldBeFound("evrakno.in=" + DEFAULT_EVRAKNO + "," + UPDATED_EVRAKNO);

        // Get all the cariList where evrakno equals to UPDATED_EVRAKNO
        defaultCariShouldNotBeFound("evrakno.in=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllCarisByEvraknoIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where evrakno is not null
        defaultCariShouldBeFound("evrakno.specified=true");

        // Get all the cariList where evrakno is null
        defaultCariShouldNotBeFound("evrakno.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByEvraknoContainsSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where evrakno contains DEFAULT_EVRAKNO
        defaultCariShouldBeFound("evrakno.contains=" + DEFAULT_EVRAKNO);

        // Get all the cariList where evrakno contains UPDATED_EVRAKNO
        defaultCariShouldNotBeFound("evrakno.contains=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllCarisByEvraknoNotContainsSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where evrakno does not contain DEFAULT_EVRAKNO
        defaultCariShouldNotBeFound("evrakno.doesNotContain=" + DEFAULT_EVRAKNO);

        // Get all the cariList where evrakno does not contain UPDATED_EVRAKNO
        defaultCariShouldBeFound("evrakno.doesNotContain=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllCarisByDepotipiIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where depotipi equals to DEFAULT_DEPOTIPI
        defaultCariShouldBeFound("depotipi.equals=" + DEFAULT_DEPOTIPI);

        // Get all the cariList where depotipi equals to UPDATED_DEPOTIPI
        defaultCariShouldNotBeFound("depotipi.equals=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCarisByDepotipiIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where depotipi in DEFAULT_DEPOTIPI or UPDATED_DEPOTIPI
        defaultCariShouldBeFound("depotipi.in=" + DEFAULT_DEPOTIPI + "," + UPDATED_DEPOTIPI);

        // Get all the cariList where depotipi equals to UPDATED_DEPOTIPI
        defaultCariShouldNotBeFound("depotipi.in=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllCarisByDepotipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where depotipi is not null
        defaultCariShouldBeFound("depotipi.specified=true");

        // Get all the cariList where depotipi is null
        defaultCariShouldNotBeFound("depotipi.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih equals to DEFAULT_TARIH
        defaultCariShouldBeFound("tarih.equals=" + DEFAULT_TARIH);

        // Get all the cariList where tarih equals to UPDATED_TARIH
        defaultCariShouldNotBeFound("tarih.equals=" + UPDATED_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih in DEFAULT_TARIH or UPDATED_TARIH
        defaultCariShouldBeFound("tarih.in=" + DEFAULT_TARIH + "," + UPDATED_TARIH);

        // Get all the cariList where tarih equals to UPDATED_TARIH
        defaultCariShouldNotBeFound("tarih.in=" + UPDATED_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih is not null
        defaultCariShouldBeFound("tarih.specified=true");

        // Get all the cariList where tarih is null
        defaultCariShouldNotBeFound("tarih.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih is greater than or equal to DEFAULT_TARIH
        defaultCariShouldBeFound("tarih.greaterThanOrEqual=" + DEFAULT_TARIH);

        // Get all the cariList where tarih is greater than or equal to UPDATED_TARIH
        defaultCariShouldNotBeFound("tarih.greaterThanOrEqual=" + UPDATED_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih is less than or equal to DEFAULT_TARIH
        defaultCariShouldBeFound("tarih.lessThanOrEqual=" + DEFAULT_TARIH);

        // Get all the cariList where tarih is less than or equal to SMALLER_TARIH
        defaultCariShouldNotBeFound("tarih.lessThanOrEqual=" + SMALLER_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsLessThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih is less than DEFAULT_TARIH
        defaultCariShouldNotBeFound("tarih.lessThan=" + DEFAULT_TARIH);

        // Get all the cariList where tarih is less than UPDATED_TARIH
        defaultCariShouldBeFound("tarih.lessThan=" + UPDATED_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByTarihIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where tarih is greater than DEFAULT_TARIH
        defaultCariShouldNotBeFound("tarih.greaterThan=" + DEFAULT_TARIH);

        // Get all the cariList where tarih is greater than SMALLER_TARIH
        defaultCariShouldBeFound("tarih.greaterThan=" + SMALLER_TARIH);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade equals to DEFAULT_VADE
        defaultCariShouldBeFound("vade.equals=" + DEFAULT_VADE);

        // Get all the cariList where vade equals to UPDATED_VADE
        defaultCariShouldNotBeFound("vade.equals=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade in DEFAULT_VADE or UPDATED_VADE
        defaultCariShouldBeFound("vade.in=" + DEFAULT_VADE + "," + UPDATED_VADE);

        // Get all the cariList where vade equals to UPDATED_VADE
        defaultCariShouldNotBeFound("vade.in=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade is not null
        defaultCariShouldBeFound("vade.specified=true");

        // Get all the cariList where vade is null
        defaultCariShouldNotBeFound("vade.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade is greater than or equal to DEFAULT_VADE
        defaultCariShouldBeFound("vade.greaterThanOrEqual=" + DEFAULT_VADE);

        // Get all the cariList where vade is greater than or equal to UPDATED_VADE
        defaultCariShouldNotBeFound("vade.greaterThanOrEqual=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade is less than or equal to DEFAULT_VADE
        defaultCariShouldBeFound("vade.lessThanOrEqual=" + DEFAULT_VADE);

        // Get all the cariList where vade is less than or equal to SMALLER_VADE
        defaultCariShouldNotBeFound("vade.lessThanOrEqual=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsLessThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade is less than DEFAULT_VADE
        defaultCariShouldNotBeFound("vade.lessThan=" + DEFAULT_VADE);

        // Get all the cariList where vade is less than UPDATED_VADE
        defaultCariShouldBeFound("vade.lessThan=" + UPDATED_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByVadeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where vade is greater than DEFAULT_VADE
        defaultCariShouldNotBeFound("vade.greaterThan=" + DEFAULT_VADE);

        // Get all the cariList where vade is greater than SMALLER_VADE
        defaultCariShouldBeFound("vade.greaterThan=" + SMALLER_VADE);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun equals to DEFAULT_GUN
        defaultCariShouldBeFound("gun.equals=" + DEFAULT_GUN);

        // Get all the cariList where gun equals to UPDATED_GUN
        defaultCariShouldNotBeFound("gun.equals=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun in DEFAULT_GUN or UPDATED_GUN
        defaultCariShouldBeFound("gun.in=" + DEFAULT_GUN + "," + UPDATED_GUN);

        // Get all the cariList where gun equals to UPDATED_GUN
        defaultCariShouldNotBeFound("gun.in=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun is not null
        defaultCariShouldBeFound("gun.specified=true");

        // Get all the cariList where gun is null
        defaultCariShouldNotBeFound("gun.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByGunIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun is greater than or equal to DEFAULT_GUN
        defaultCariShouldBeFound("gun.greaterThanOrEqual=" + DEFAULT_GUN);

        // Get all the cariList where gun is greater than or equal to UPDATED_GUN
        defaultCariShouldNotBeFound("gun.greaterThanOrEqual=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun is less than or equal to DEFAULT_GUN
        defaultCariShouldBeFound("gun.lessThanOrEqual=" + DEFAULT_GUN);

        // Get all the cariList where gun is less than or equal to SMALLER_GUN
        defaultCariShouldNotBeFound("gun.lessThanOrEqual=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsLessThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun is less than DEFAULT_GUN
        defaultCariShouldNotBeFound("gun.lessThan=" + DEFAULT_GUN);

        // Get all the cariList where gun is less than UPDATED_GUN
        defaultCariShouldBeFound("gun.lessThan=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByGunIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where gun is greater than DEFAULT_GUN
        defaultCariShouldNotBeFound("gun.greaterThan=" + DEFAULT_GUN);

        // Get all the cariList where gun is greater than SMALLER_GUN
        defaultCariShouldBeFound("gun.greaterThan=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllCarisByAciklamaIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where aciklama equals to DEFAULT_ACIKLAMA
        defaultCariShouldBeFound("aciklama.equals=" + DEFAULT_ACIKLAMA);

        // Get all the cariList where aciklama equals to UPDATED_ACIKLAMA
        defaultCariShouldNotBeFound("aciklama.equals=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCarisByAciklamaIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where aciklama in DEFAULT_ACIKLAMA or UPDATED_ACIKLAMA
        defaultCariShouldBeFound("aciklama.in=" + DEFAULT_ACIKLAMA + "," + UPDATED_ACIKLAMA);

        // Get all the cariList where aciklama equals to UPDATED_ACIKLAMA
        defaultCariShouldNotBeFound("aciklama.in=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCarisByAciklamaIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where aciklama is not null
        defaultCariShouldBeFound("aciklama.specified=true");

        // Get all the cariList where aciklama is null
        defaultCariShouldNotBeFound("aciklama.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByAciklamaContainsSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where aciklama contains DEFAULT_ACIKLAMA
        defaultCariShouldBeFound("aciklama.contains=" + DEFAULT_ACIKLAMA);

        // Get all the cariList where aciklama contains UPDATED_ACIKLAMA
        defaultCariShouldNotBeFound("aciklama.contains=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCarisByAciklamaNotContainsSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where aciklama does not contain DEFAULT_ACIKLAMA
        defaultCariShouldNotBeFound("aciklama.doesNotContain=" + DEFAULT_ACIKLAMA);

        // Get all the cariList where aciklama does not contain UPDATED_ACIKLAMA
        defaultCariShouldBeFound("aciklama.doesNotContain=" + UPDATED_ACIKLAMA);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc equals to DEFAULT_BORC
        defaultCariShouldBeFound("borc.equals=" + DEFAULT_BORC);

        // Get all the cariList where borc equals to UPDATED_BORC
        defaultCariShouldNotBeFound("borc.equals=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc in DEFAULT_BORC or UPDATED_BORC
        defaultCariShouldBeFound("borc.in=" + DEFAULT_BORC + "," + UPDATED_BORC);

        // Get all the cariList where borc equals to UPDATED_BORC
        defaultCariShouldNotBeFound("borc.in=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc is not null
        defaultCariShouldBeFound("borc.specified=true");

        // Get all the cariList where borc is null
        defaultCariShouldNotBeFound("borc.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc is greater than or equal to DEFAULT_BORC
        defaultCariShouldBeFound("borc.greaterThanOrEqual=" + DEFAULT_BORC);

        // Get all the cariList where borc is greater than or equal to UPDATED_BORC
        defaultCariShouldNotBeFound("borc.greaterThanOrEqual=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc is less than or equal to DEFAULT_BORC
        defaultCariShouldBeFound("borc.lessThanOrEqual=" + DEFAULT_BORC);

        // Get all the cariList where borc is less than or equal to SMALLER_BORC
        defaultCariShouldNotBeFound("borc.lessThanOrEqual=" + SMALLER_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsLessThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc is less than DEFAULT_BORC
        defaultCariShouldNotBeFound("borc.lessThan=" + DEFAULT_BORC);

        // Get all the cariList where borc is less than UPDATED_BORC
        defaultCariShouldBeFound("borc.lessThan=" + UPDATED_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByBorcIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where borc is greater than DEFAULT_BORC
        defaultCariShouldNotBeFound("borc.greaterThan=" + DEFAULT_BORC);

        // Get all the cariList where borc is greater than SMALLER_BORC
        defaultCariShouldBeFound("borc.greaterThan=" + SMALLER_BORC);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak equals to DEFAULT_ALACAK
        defaultCariShouldBeFound("alacak.equals=" + DEFAULT_ALACAK);

        // Get all the cariList where alacak equals to UPDATED_ALACAK
        defaultCariShouldNotBeFound("alacak.equals=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsInShouldWork() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak in DEFAULT_ALACAK or UPDATED_ALACAK
        defaultCariShouldBeFound("alacak.in=" + DEFAULT_ALACAK + "," + UPDATED_ALACAK);

        // Get all the cariList where alacak equals to UPDATED_ALACAK
        defaultCariShouldNotBeFound("alacak.in=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsNullOrNotNull() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak is not null
        defaultCariShouldBeFound("alacak.specified=true");

        // Get all the cariList where alacak is null
        defaultCariShouldNotBeFound("alacak.specified=false");
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak is greater than or equal to DEFAULT_ALACAK
        defaultCariShouldBeFound("alacak.greaterThanOrEqual=" + DEFAULT_ALACAK);

        // Get all the cariList where alacak is greater than or equal to UPDATED_ALACAK
        defaultCariShouldNotBeFound("alacak.greaterThanOrEqual=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak is less than or equal to DEFAULT_ALACAK
        defaultCariShouldBeFound("alacak.lessThanOrEqual=" + DEFAULT_ALACAK);

        // Get all the cariList where alacak is less than or equal to SMALLER_ALACAK
        defaultCariShouldNotBeFound("alacak.lessThanOrEqual=" + SMALLER_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsLessThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak is less than DEFAULT_ALACAK
        defaultCariShouldNotBeFound("alacak.lessThan=" + DEFAULT_ALACAK);

        // Get all the cariList where alacak is less than UPDATED_ALACAK
        defaultCariShouldBeFound("alacak.lessThan=" + UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisByAlacakIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        // Get all the cariList where alacak is greater than DEFAULT_ALACAK
        defaultCariShouldNotBeFound("alacak.greaterThan=" + DEFAULT_ALACAK);

        // Get all the cariList where alacak is greater than SMALLER_ALACAK
        defaultCariShouldBeFound("alacak.greaterThan=" + SMALLER_ALACAK);
    }

    @Test
    @Transactional
    void getAllCarisBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            cariRepository.saveAndFlush(cari);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        cari.setSicil(sicil);
        cariRepository.saveAndFlush(cari);
        Long sicilId = sicil.getId();

        // Get all the cariList where sicil equals to sicilId
        defaultCariShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the cariList where sicil equals to (sicilId + 1)
        defaultCariShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCariShouldBeFound(String filter) throws Exception {
        restCariMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cari.getId().intValue())))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI.toString())))
            .andExpect(jsonPath("$.[*].evrakno").value(hasItem(DEFAULT_EVRAKNO)))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].tarih").value(hasItem(DEFAULT_TARIH.toString())))
            .andExpect(jsonPath("$.[*].vade").value(hasItem(DEFAULT_VADE.toString())))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].aciklama").value(hasItem(DEFAULT_ACIKLAMA)))
            .andExpect(jsonPath("$.[*].borc").value(hasItem(sameNumber(DEFAULT_BORC))))
            .andExpect(jsonPath("$.[*].alacak").value(hasItem(sameNumber(DEFAULT_ALACAK))));

        // Check, that the count call also returns 1
        restCariMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCariShouldNotBeFound(String filter) throws Exception {
        restCariMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCariMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCari() throws Exception {
        // Get the cari
        restCariMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCari() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        int databaseSizeBeforeUpdate = cariRepository.findAll().size();

        // Update the cari
        Cari updatedCari = cariRepository.findById(cari.getId()).get();
        // Disconnect from session so that the updates on updatedCari are not directly saved in db
        em.detach(updatedCari);
        updatedCari
            .tipi(UPDATED_TIPI)
            .evrakno(UPDATED_EVRAKNO)
            .depotipi(UPDATED_DEPOTIPI)
            .tarih(UPDATED_TARIH)
            .vade(UPDATED_VADE)
            .gun(UPDATED_GUN)
            .aciklama(UPDATED_ACIKLAMA)
            .borc(UPDATED_BORC)
            .alacak(UPDATED_ALACAK);
        CariDTO cariDTO = cariMapper.toDto(updatedCari);

        restCariMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cariDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDTO))
            )
            .andExpect(status().isOk());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
        Cari testCari = cariList.get(cariList.size() - 1);
        assertThat(testCari.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testCari.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testCari.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCari.getTarih()).isEqualTo(UPDATED_TARIH);
        assertThat(testCari.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCari.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testCari.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testCari.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCari.getAlacak()).isEqualByComparingTo(UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void putNonExistingCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cariDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cariDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cariDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCariWithPatch() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        int databaseSizeBeforeUpdate = cariRepository.findAll().size();

        // Update the cari using partial update
        Cari partialUpdatedCari = new Cari();
        partialUpdatedCari.setId(cari.getId());

        partialUpdatedCari
            .evrakno(UPDATED_EVRAKNO)
            .depotipi(UPDATED_DEPOTIPI)
            .tarih(UPDATED_TARIH)
            .vade(UPDATED_VADE)
            .borc(UPDATED_BORC)
            .alacak(UPDATED_ALACAK);

        restCariMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCari.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCari))
            )
            .andExpect(status().isOk());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
        Cari testCari = cariList.get(cariList.size() - 1);
        assertThat(testCari.getTipi()).isEqualTo(DEFAULT_TIPI);
        assertThat(testCari.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testCari.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCari.getTarih()).isEqualTo(UPDATED_TARIH);
        assertThat(testCari.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCari.getGun()).isEqualTo(DEFAULT_GUN);
        assertThat(testCari.getAciklama()).isEqualTo(DEFAULT_ACIKLAMA);
        assertThat(testCari.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCari.getAlacak()).isEqualByComparingTo(UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void fullUpdateCariWithPatch() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        int databaseSizeBeforeUpdate = cariRepository.findAll().size();

        // Update the cari using partial update
        Cari partialUpdatedCari = new Cari();
        partialUpdatedCari.setId(cari.getId());

        partialUpdatedCari
            .tipi(UPDATED_TIPI)
            .evrakno(UPDATED_EVRAKNO)
            .depotipi(UPDATED_DEPOTIPI)
            .tarih(UPDATED_TARIH)
            .vade(UPDATED_VADE)
            .gun(UPDATED_GUN)
            .aciklama(UPDATED_ACIKLAMA)
            .borc(UPDATED_BORC)
            .alacak(UPDATED_ALACAK);

        restCariMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCari.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCari))
            )
            .andExpect(status().isOk());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
        Cari testCari = cariList.get(cariList.size() - 1);
        assertThat(testCari.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testCari.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testCari.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testCari.getTarih()).isEqualTo(UPDATED_TARIH);
        assertThat(testCari.getVade()).isEqualTo(UPDATED_VADE);
        assertThat(testCari.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testCari.getAciklama()).isEqualTo(UPDATED_ACIKLAMA);
        assertThat(testCari.getBorc()).isEqualByComparingTo(UPDATED_BORC);
        assertThat(testCari.getAlacak()).isEqualByComparingTo(UPDATED_ALACAK);
    }

    @Test
    @Transactional
    void patchNonExistingCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cariDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cariDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cariDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCari() throws Exception {
        int databaseSizeBeforeUpdate = cariRepository.findAll().size();
        cari.setId(count.incrementAndGet());

        // Create the Cari
        CariDTO cariDTO = cariMapper.toDto(cari);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCariMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cariDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cari in the database
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCari() throws Exception {
        // Initialize the database
        cariRepository.saveAndFlush(cari);

        int databaseSizeBeforeDelete = cariRepository.findAll().size();

        // Delete the cari
        restCariMockMvc
            .perform(delete(ENTITY_API_URL_ID, cari.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Cari> cariList = cariRepository.findAll();
        assertThat(cariList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
