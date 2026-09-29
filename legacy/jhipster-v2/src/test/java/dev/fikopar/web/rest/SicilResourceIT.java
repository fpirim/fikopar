package dev.fikopar.web.rest;

import static dev.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.fikopar.IntegrationTest;
import dev.fikopar.domain.Cari;
import dev.fikopar.domain.Sicil;
import dev.fikopar.domain.enumeration.SicilTipi;
import dev.fikopar.repository.SicilRepository;
import dev.fikopar.service.criteria.SicilCriteria;
import dev.fikopar.service.dto.SicilDTO;
import dev.fikopar.service.mapper.SicilMapper;
import java.math.BigDecimal;
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
 * Integration tests for the {@link SicilResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SicilResourceIT {

    private static final String DEFAULT_HESAPNO = "AAAAAAAAAA";
    private static final String UPDATED_HESAPNO = "BBBBBBBBBB";

    private static final String DEFAULT_HESAPADI = "AAAAAAAAAA";
    private static final String UPDATED_HESAPADI = "BBBBBBBBBB";

    private static final String DEFAULT_ADRESI = "AAAAAAAAAA";
    private static final String UPDATED_ADRESI = "BBBBBBBBBB";

    private static final String DEFAULT_ILCE = "AAAAAAAAAA";
    private static final String UPDATED_ILCE = "BBBBBBBBBB";

    private static final String DEFAULT_SEHIR = "AAAAAAAAAA";
    private static final String UPDATED_SEHIR = "BBBBBBBBBB";

    private static final String DEFAULT_TELEFON = "AAAAAAAAAA";
    private static final String UPDATED_TELEFON = "BBBBBBBBBB";

    private static final String DEFAULT_VDA = "AAAAAAAAAA";
    private static final String UPDATED_VDA = "BBBBBBBBBB";

    private static final String DEFAULT_VNO = "AAAAAAAAAA";
    private static final String UPDATED_VNO = "BBBBBBBBBB";

    private static final String DEFAULT_TCNO = "AAAAAAAAAA";
    private static final String UPDATED_TCNO = "BBBBBBBBBB";

    private static final Integer DEFAULT_GUN = 1;
    private static final Integer UPDATED_GUN = 2;
    private static final Integer SMALLER_GUN = 1 - 1;

    private static final BigDecimal DEFAULT_ISKONTO = new BigDecimal(1);
    private static final BigDecimal UPDATED_ISKONTO = new BigDecimal(2);
    private static final BigDecimal SMALLER_ISKONTO = new BigDecimal(1 - 1);

    private static final SicilTipi DEFAULT_SICILTIPI = SicilTipi.MUSTERI;
    private static final SicilTipi UPDATED_SICILTIPI = SicilTipi.FIRMA;

    private static final String ENTITY_API_URL = "/api/sicils";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SicilRepository sicilRepository;

    @Autowired
    private SicilMapper sicilMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSicilMockMvc;

    private Sicil sicil;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sicil createEntity(EntityManager em) {
        Sicil sicil = new Sicil()
            .hesapno(DEFAULT_HESAPNO)
            .hesapadi(DEFAULT_HESAPADI)
            .adresi(DEFAULT_ADRESI)
            .ilce(DEFAULT_ILCE)
            .sehir(DEFAULT_SEHIR)
            .telefon(DEFAULT_TELEFON)
            .vda(DEFAULT_VDA)
            .vno(DEFAULT_VNO)
            .tcno(DEFAULT_TCNO)
            .gun(DEFAULT_GUN)
            .iskonto(DEFAULT_ISKONTO)
            .siciltipi(DEFAULT_SICILTIPI);
        return sicil;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sicil createUpdatedEntity(EntityManager em) {
        Sicil sicil = new Sicil()
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .telefon(UPDATED_TELEFON)
            .vda(UPDATED_VDA)
            .vno(UPDATED_VNO)
            .tcno(UPDATED_TCNO)
            .gun(UPDATED_GUN)
            .iskonto(UPDATED_ISKONTO)
            .siciltipi(UPDATED_SICILTIPI);
        return sicil;
    }

    @BeforeEach
    public void initTest() {
        sicil = createEntity(em);
    }

    @Test
    @Transactional
    void createSicil() throws Exception {
        int databaseSizeBeforeCreate = sicilRepository.findAll().size();
        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);
        restSicilMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(sicilDTO)))
            .andExpect(status().isCreated());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeCreate + 1);
        Sicil testSicil = sicilList.get(sicilList.size() - 1);
        assertThat(testSicil.getHesapno()).isEqualTo(DEFAULT_HESAPNO);
        assertThat(testSicil.getHesapadi()).isEqualTo(DEFAULT_HESAPADI);
        assertThat(testSicil.getAdresi()).isEqualTo(DEFAULT_ADRESI);
        assertThat(testSicil.getIlce()).isEqualTo(DEFAULT_ILCE);
        assertThat(testSicil.getSehir()).isEqualTo(DEFAULT_SEHIR);
        assertThat(testSicil.getTelefon()).isEqualTo(DEFAULT_TELEFON);
        assertThat(testSicil.getVda()).isEqualTo(DEFAULT_VDA);
        assertThat(testSicil.getVno()).isEqualTo(DEFAULT_VNO);
        assertThat(testSicil.getTcno()).isEqualTo(DEFAULT_TCNO);
        assertThat(testSicil.getGun()).isEqualTo(DEFAULT_GUN);
        assertThat(testSicil.getIskonto()).isEqualByComparingTo(DEFAULT_ISKONTO);
        assertThat(testSicil.getSiciltipi()).isEqualTo(DEFAULT_SICILTIPI);
    }

    @Test
    @Transactional
    void createSicilWithExistingId() throws Exception {
        // Create the Sicil with an existing ID
        sicil.setId(1L);
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        int databaseSizeBeforeCreate = sicilRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSicilMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(sicilDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllSicils() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList
        restSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sicil.getId().intValue())))
            .andExpect(jsonPath("$.[*].hesapno").value(hasItem(DEFAULT_HESAPNO)))
            .andExpect(jsonPath("$.[*].hesapadi").value(hasItem(DEFAULT_HESAPADI)))
            .andExpect(jsonPath("$.[*].adresi").value(hasItem(DEFAULT_ADRESI)))
            .andExpect(jsonPath("$.[*].ilce").value(hasItem(DEFAULT_ILCE)))
            .andExpect(jsonPath("$.[*].sehir").value(hasItem(DEFAULT_SEHIR)))
            .andExpect(jsonPath("$.[*].telefon").value(hasItem(DEFAULT_TELEFON)))
            .andExpect(jsonPath("$.[*].vda").value(hasItem(DEFAULT_VDA)))
            .andExpect(jsonPath("$.[*].vno").value(hasItem(DEFAULT_VNO)))
            .andExpect(jsonPath("$.[*].tcno").value(hasItem(DEFAULT_TCNO)))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].iskonto").value(hasItem(sameNumber(DEFAULT_ISKONTO))))
            .andExpect(jsonPath("$.[*].siciltipi").value(hasItem(DEFAULT_SICILTIPI.toString())));
    }

    @Test
    @Transactional
    void getSicil() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get the sicil
        restSicilMockMvc
            .perform(get(ENTITY_API_URL_ID, sicil.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sicil.getId().intValue()))
            .andExpect(jsonPath("$.hesapno").value(DEFAULT_HESAPNO))
            .andExpect(jsonPath("$.hesapadi").value(DEFAULT_HESAPADI))
            .andExpect(jsonPath("$.adresi").value(DEFAULT_ADRESI))
            .andExpect(jsonPath("$.ilce").value(DEFAULT_ILCE))
            .andExpect(jsonPath("$.sehir").value(DEFAULT_SEHIR))
            .andExpect(jsonPath("$.telefon").value(DEFAULT_TELEFON))
            .andExpect(jsonPath("$.vda").value(DEFAULT_VDA))
            .andExpect(jsonPath("$.vno").value(DEFAULT_VNO))
            .andExpect(jsonPath("$.tcno").value(DEFAULT_TCNO))
            .andExpect(jsonPath("$.gun").value(DEFAULT_GUN))
            .andExpect(jsonPath("$.iskonto").value(sameNumber(DEFAULT_ISKONTO)))
            .andExpect(jsonPath("$.siciltipi").value(DEFAULT_SICILTIPI.toString()));
    }

    @Test
    @Transactional
    void getSicilsByIdFiltering() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        Long id = sicil.getId();

        defaultSicilShouldBeFound("id.equals=" + id);
        defaultSicilShouldNotBeFound("id.notEquals=" + id);

        defaultSicilShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultSicilShouldNotBeFound("id.greaterThan=" + id);

        defaultSicilShouldBeFound("id.lessThanOrEqual=" + id);
        defaultSicilShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapnoIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapno equals to DEFAULT_HESAPNO
        defaultSicilShouldBeFound("hesapno.equals=" + DEFAULT_HESAPNO);

        // Get all the sicilList where hesapno equals to UPDATED_HESAPNO
        defaultSicilShouldNotBeFound("hesapno.equals=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapnoIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapno in DEFAULT_HESAPNO or UPDATED_HESAPNO
        defaultSicilShouldBeFound("hesapno.in=" + DEFAULT_HESAPNO + "," + UPDATED_HESAPNO);

        // Get all the sicilList where hesapno equals to UPDATED_HESAPNO
        defaultSicilShouldNotBeFound("hesapno.in=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapno is not null
        defaultSicilShouldBeFound("hesapno.specified=true");

        // Get all the sicilList where hesapno is null
        defaultSicilShouldNotBeFound("hesapno.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByHesapnoContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapno contains DEFAULT_HESAPNO
        defaultSicilShouldBeFound("hesapno.contains=" + DEFAULT_HESAPNO);

        // Get all the sicilList where hesapno contains UPDATED_HESAPNO
        defaultSicilShouldNotBeFound("hesapno.contains=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapnoNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapno does not contain DEFAULT_HESAPNO
        defaultSicilShouldNotBeFound("hesapno.doesNotContain=" + DEFAULT_HESAPNO);

        // Get all the sicilList where hesapno does not contain UPDATED_HESAPNO
        defaultSicilShouldBeFound("hesapno.doesNotContain=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapadiIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapadi equals to DEFAULT_HESAPADI
        defaultSicilShouldBeFound("hesapadi.equals=" + DEFAULT_HESAPADI);

        // Get all the sicilList where hesapadi equals to UPDATED_HESAPADI
        defaultSicilShouldNotBeFound("hesapadi.equals=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapadiIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapadi in DEFAULT_HESAPADI or UPDATED_HESAPADI
        defaultSicilShouldBeFound("hesapadi.in=" + DEFAULT_HESAPADI + "," + UPDATED_HESAPADI);

        // Get all the sicilList where hesapadi equals to UPDATED_HESAPADI
        defaultSicilShouldNotBeFound("hesapadi.in=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapadiIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapadi is not null
        defaultSicilShouldBeFound("hesapadi.specified=true");

        // Get all the sicilList where hesapadi is null
        defaultSicilShouldNotBeFound("hesapadi.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByHesapadiContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapadi contains DEFAULT_HESAPADI
        defaultSicilShouldBeFound("hesapadi.contains=" + DEFAULT_HESAPADI);

        // Get all the sicilList where hesapadi contains UPDATED_HESAPADI
        defaultSicilShouldNotBeFound("hesapadi.contains=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllSicilsByHesapadiNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where hesapadi does not contain DEFAULT_HESAPADI
        defaultSicilShouldNotBeFound("hesapadi.doesNotContain=" + DEFAULT_HESAPADI);

        // Get all the sicilList where hesapadi does not contain UPDATED_HESAPADI
        defaultSicilShouldBeFound("hesapadi.doesNotContain=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllSicilsByAdresiIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where adresi equals to DEFAULT_ADRESI
        defaultSicilShouldBeFound("adresi.equals=" + DEFAULT_ADRESI);

        // Get all the sicilList where adresi equals to UPDATED_ADRESI
        defaultSicilShouldNotBeFound("adresi.equals=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllSicilsByAdresiIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where adresi in DEFAULT_ADRESI or UPDATED_ADRESI
        defaultSicilShouldBeFound("adresi.in=" + DEFAULT_ADRESI + "," + UPDATED_ADRESI);

        // Get all the sicilList where adresi equals to UPDATED_ADRESI
        defaultSicilShouldNotBeFound("adresi.in=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllSicilsByAdresiIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where adresi is not null
        defaultSicilShouldBeFound("adresi.specified=true");

        // Get all the sicilList where adresi is null
        defaultSicilShouldNotBeFound("adresi.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByAdresiContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where adresi contains DEFAULT_ADRESI
        defaultSicilShouldBeFound("adresi.contains=" + DEFAULT_ADRESI);

        // Get all the sicilList where adresi contains UPDATED_ADRESI
        defaultSicilShouldNotBeFound("adresi.contains=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllSicilsByAdresiNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where adresi does not contain DEFAULT_ADRESI
        defaultSicilShouldNotBeFound("adresi.doesNotContain=" + DEFAULT_ADRESI);

        // Get all the sicilList where adresi does not contain UPDATED_ADRESI
        defaultSicilShouldBeFound("adresi.doesNotContain=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllSicilsByIlceIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where ilce equals to DEFAULT_ILCE
        defaultSicilShouldBeFound("ilce.equals=" + DEFAULT_ILCE);

        // Get all the sicilList where ilce equals to UPDATED_ILCE
        defaultSicilShouldNotBeFound("ilce.equals=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllSicilsByIlceIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where ilce in DEFAULT_ILCE or UPDATED_ILCE
        defaultSicilShouldBeFound("ilce.in=" + DEFAULT_ILCE + "," + UPDATED_ILCE);

        // Get all the sicilList where ilce equals to UPDATED_ILCE
        defaultSicilShouldNotBeFound("ilce.in=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllSicilsByIlceIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where ilce is not null
        defaultSicilShouldBeFound("ilce.specified=true");

        // Get all the sicilList where ilce is null
        defaultSicilShouldNotBeFound("ilce.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByIlceContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where ilce contains DEFAULT_ILCE
        defaultSicilShouldBeFound("ilce.contains=" + DEFAULT_ILCE);

        // Get all the sicilList where ilce contains UPDATED_ILCE
        defaultSicilShouldNotBeFound("ilce.contains=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllSicilsByIlceNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where ilce does not contain DEFAULT_ILCE
        defaultSicilShouldNotBeFound("ilce.doesNotContain=" + DEFAULT_ILCE);

        // Get all the sicilList where ilce does not contain UPDATED_ILCE
        defaultSicilShouldBeFound("ilce.doesNotContain=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllSicilsBySehirIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where sehir equals to DEFAULT_SEHIR
        defaultSicilShouldBeFound("sehir.equals=" + DEFAULT_SEHIR);

        // Get all the sicilList where sehir equals to UPDATED_SEHIR
        defaultSicilShouldNotBeFound("sehir.equals=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllSicilsBySehirIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where sehir in DEFAULT_SEHIR or UPDATED_SEHIR
        defaultSicilShouldBeFound("sehir.in=" + DEFAULT_SEHIR + "," + UPDATED_SEHIR);

        // Get all the sicilList where sehir equals to UPDATED_SEHIR
        defaultSicilShouldNotBeFound("sehir.in=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllSicilsBySehirIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where sehir is not null
        defaultSicilShouldBeFound("sehir.specified=true");

        // Get all the sicilList where sehir is null
        defaultSicilShouldNotBeFound("sehir.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsBySehirContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where sehir contains DEFAULT_SEHIR
        defaultSicilShouldBeFound("sehir.contains=" + DEFAULT_SEHIR);

        // Get all the sicilList where sehir contains UPDATED_SEHIR
        defaultSicilShouldNotBeFound("sehir.contains=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllSicilsBySehirNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where sehir does not contain DEFAULT_SEHIR
        defaultSicilShouldNotBeFound("sehir.doesNotContain=" + DEFAULT_SEHIR);

        // Get all the sicilList where sehir does not contain UPDATED_SEHIR
        defaultSicilShouldBeFound("sehir.doesNotContain=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllSicilsByTelefonIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where telefon equals to DEFAULT_TELEFON
        defaultSicilShouldBeFound("telefon.equals=" + DEFAULT_TELEFON);

        // Get all the sicilList where telefon equals to UPDATED_TELEFON
        defaultSicilShouldNotBeFound("telefon.equals=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllSicilsByTelefonIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where telefon in DEFAULT_TELEFON or UPDATED_TELEFON
        defaultSicilShouldBeFound("telefon.in=" + DEFAULT_TELEFON + "," + UPDATED_TELEFON);

        // Get all the sicilList where telefon equals to UPDATED_TELEFON
        defaultSicilShouldNotBeFound("telefon.in=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllSicilsByTelefonIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where telefon is not null
        defaultSicilShouldBeFound("telefon.specified=true");

        // Get all the sicilList where telefon is null
        defaultSicilShouldNotBeFound("telefon.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByTelefonContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where telefon contains DEFAULT_TELEFON
        defaultSicilShouldBeFound("telefon.contains=" + DEFAULT_TELEFON);

        // Get all the sicilList where telefon contains UPDATED_TELEFON
        defaultSicilShouldNotBeFound("telefon.contains=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllSicilsByTelefonNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where telefon does not contain DEFAULT_TELEFON
        defaultSicilShouldNotBeFound("telefon.doesNotContain=" + DEFAULT_TELEFON);

        // Get all the sicilList where telefon does not contain UPDATED_TELEFON
        defaultSicilShouldBeFound("telefon.doesNotContain=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllSicilsByVdaIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vda equals to DEFAULT_VDA
        defaultSicilShouldBeFound("vda.equals=" + DEFAULT_VDA);

        // Get all the sicilList where vda equals to UPDATED_VDA
        defaultSicilShouldNotBeFound("vda.equals=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllSicilsByVdaIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vda in DEFAULT_VDA or UPDATED_VDA
        defaultSicilShouldBeFound("vda.in=" + DEFAULT_VDA + "," + UPDATED_VDA);

        // Get all the sicilList where vda equals to UPDATED_VDA
        defaultSicilShouldNotBeFound("vda.in=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllSicilsByVdaIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vda is not null
        defaultSicilShouldBeFound("vda.specified=true");

        // Get all the sicilList where vda is null
        defaultSicilShouldNotBeFound("vda.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByVdaContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vda contains DEFAULT_VDA
        defaultSicilShouldBeFound("vda.contains=" + DEFAULT_VDA);

        // Get all the sicilList where vda contains UPDATED_VDA
        defaultSicilShouldNotBeFound("vda.contains=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllSicilsByVdaNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vda does not contain DEFAULT_VDA
        defaultSicilShouldNotBeFound("vda.doesNotContain=" + DEFAULT_VDA);

        // Get all the sicilList where vda does not contain UPDATED_VDA
        defaultSicilShouldBeFound("vda.doesNotContain=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllSicilsByVnoIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vno equals to DEFAULT_VNO
        defaultSicilShouldBeFound("vno.equals=" + DEFAULT_VNO);

        // Get all the sicilList where vno equals to UPDATED_VNO
        defaultSicilShouldNotBeFound("vno.equals=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllSicilsByVnoIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vno in DEFAULT_VNO or UPDATED_VNO
        defaultSicilShouldBeFound("vno.in=" + DEFAULT_VNO + "," + UPDATED_VNO);

        // Get all the sicilList where vno equals to UPDATED_VNO
        defaultSicilShouldNotBeFound("vno.in=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllSicilsByVnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vno is not null
        defaultSicilShouldBeFound("vno.specified=true");

        // Get all the sicilList where vno is null
        defaultSicilShouldNotBeFound("vno.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByVnoContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vno contains DEFAULT_VNO
        defaultSicilShouldBeFound("vno.contains=" + DEFAULT_VNO);

        // Get all the sicilList where vno contains UPDATED_VNO
        defaultSicilShouldNotBeFound("vno.contains=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllSicilsByVnoNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where vno does not contain DEFAULT_VNO
        defaultSicilShouldNotBeFound("vno.doesNotContain=" + DEFAULT_VNO);

        // Get all the sicilList where vno does not contain UPDATED_VNO
        defaultSicilShouldBeFound("vno.doesNotContain=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllSicilsByTcnoIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where tcno equals to DEFAULT_TCNO
        defaultSicilShouldBeFound("tcno.equals=" + DEFAULT_TCNO);

        // Get all the sicilList where tcno equals to UPDATED_TCNO
        defaultSicilShouldNotBeFound("tcno.equals=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllSicilsByTcnoIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where tcno in DEFAULT_TCNO or UPDATED_TCNO
        defaultSicilShouldBeFound("tcno.in=" + DEFAULT_TCNO + "," + UPDATED_TCNO);

        // Get all the sicilList where tcno equals to UPDATED_TCNO
        defaultSicilShouldNotBeFound("tcno.in=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllSicilsByTcnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where tcno is not null
        defaultSicilShouldBeFound("tcno.specified=true");

        // Get all the sicilList where tcno is null
        defaultSicilShouldNotBeFound("tcno.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByTcnoContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where tcno contains DEFAULT_TCNO
        defaultSicilShouldBeFound("tcno.contains=" + DEFAULT_TCNO);

        // Get all the sicilList where tcno contains UPDATED_TCNO
        defaultSicilShouldNotBeFound("tcno.contains=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllSicilsByTcnoNotContainsSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where tcno does not contain DEFAULT_TCNO
        defaultSicilShouldNotBeFound("tcno.doesNotContain=" + DEFAULT_TCNO);

        // Get all the sicilList where tcno does not contain UPDATED_TCNO
        defaultSicilShouldBeFound("tcno.doesNotContain=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun equals to DEFAULT_GUN
        defaultSicilShouldBeFound("gun.equals=" + DEFAULT_GUN);

        // Get all the sicilList where gun equals to UPDATED_GUN
        defaultSicilShouldNotBeFound("gun.equals=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun in DEFAULT_GUN or UPDATED_GUN
        defaultSicilShouldBeFound("gun.in=" + DEFAULT_GUN + "," + UPDATED_GUN);

        // Get all the sicilList where gun equals to UPDATED_GUN
        defaultSicilShouldNotBeFound("gun.in=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun is not null
        defaultSicilShouldBeFound("gun.specified=true");

        // Get all the sicilList where gun is null
        defaultSicilShouldNotBeFound("gun.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun is greater than or equal to DEFAULT_GUN
        defaultSicilShouldBeFound("gun.greaterThanOrEqual=" + DEFAULT_GUN);

        // Get all the sicilList where gun is greater than or equal to UPDATED_GUN
        defaultSicilShouldNotBeFound("gun.greaterThanOrEqual=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun is less than or equal to DEFAULT_GUN
        defaultSicilShouldBeFound("gun.lessThanOrEqual=" + DEFAULT_GUN);

        // Get all the sicilList where gun is less than or equal to SMALLER_GUN
        defaultSicilShouldNotBeFound("gun.lessThanOrEqual=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsLessThanSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun is less than DEFAULT_GUN
        defaultSicilShouldNotBeFound("gun.lessThan=" + DEFAULT_GUN);

        // Get all the sicilList where gun is less than UPDATED_GUN
        defaultSicilShouldBeFound("gun.lessThan=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByGunIsGreaterThanSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where gun is greater than DEFAULT_GUN
        defaultSicilShouldNotBeFound("gun.greaterThan=" + DEFAULT_GUN);

        // Get all the sicilList where gun is greater than SMALLER_GUN
        defaultSicilShouldBeFound("gun.greaterThan=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto equals to DEFAULT_ISKONTO
        defaultSicilShouldBeFound("iskonto.equals=" + DEFAULT_ISKONTO);

        // Get all the sicilList where iskonto equals to UPDATED_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.equals=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto in DEFAULT_ISKONTO or UPDATED_ISKONTO
        defaultSicilShouldBeFound("iskonto.in=" + DEFAULT_ISKONTO + "," + UPDATED_ISKONTO);

        // Get all the sicilList where iskonto equals to UPDATED_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.in=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto is not null
        defaultSicilShouldBeFound("iskonto.specified=true");

        // Get all the sicilList where iskonto is null
        defaultSicilShouldNotBeFound("iskonto.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto is greater than or equal to DEFAULT_ISKONTO
        defaultSicilShouldBeFound("iskonto.greaterThanOrEqual=" + DEFAULT_ISKONTO);

        // Get all the sicilList where iskonto is greater than or equal to UPDATED_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.greaterThanOrEqual=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto is less than or equal to DEFAULT_ISKONTO
        defaultSicilShouldBeFound("iskonto.lessThanOrEqual=" + DEFAULT_ISKONTO);

        // Get all the sicilList where iskonto is less than or equal to SMALLER_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.lessThanOrEqual=" + SMALLER_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsLessThanSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto is less than DEFAULT_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.lessThan=" + DEFAULT_ISKONTO);

        // Get all the sicilList where iskonto is less than UPDATED_ISKONTO
        defaultSicilShouldBeFound("iskonto.lessThan=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsByIskontoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where iskonto is greater than DEFAULT_ISKONTO
        defaultSicilShouldNotBeFound("iskonto.greaterThan=" + DEFAULT_ISKONTO);

        // Get all the sicilList where iskonto is greater than SMALLER_ISKONTO
        defaultSicilShouldBeFound("iskonto.greaterThan=" + SMALLER_ISKONTO);
    }

    @Test
    @Transactional
    void getAllSicilsBySiciltipiIsEqualToSomething() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where siciltipi equals to DEFAULT_SICILTIPI
        defaultSicilShouldBeFound("siciltipi.equals=" + DEFAULT_SICILTIPI);

        // Get all the sicilList where siciltipi equals to UPDATED_SICILTIPI
        defaultSicilShouldNotBeFound("siciltipi.equals=" + UPDATED_SICILTIPI);
    }

    @Test
    @Transactional
    void getAllSicilsBySiciltipiIsInShouldWork() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where siciltipi in DEFAULT_SICILTIPI or UPDATED_SICILTIPI
        defaultSicilShouldBeFound("siciltipi.in=" + DEFAULT_SICILTIPI + "," + UPDATED_SICILTIPI);

        // Get all the sicilList where siciltipi equals to UPDATED_SICILTIPI
        defaultSicilShouldNotBeFound("siciltipi.in=" + UPDATED_SICILTIPI);
    }

    @Test
    @Transactional
    void getAllSicilsBySiciltipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        // Get all the sicilList where siciltipi is not null
        defaultSicilShouldBeFound("siciltipi.specified=true");

        // Get all the sicilList where siciltipi is null
        defaultSicilShouldNotBeFound("siciltipi.specified=false");
    }

    @Test
    @Transactional
    void getAllSicilsByCarisIsEqualToSomething() throws Exception {
        Cari caris;
        if (TestUtil.findAll(em, Cari.class).isEmpty()) {
            sicilRepository.saveAndFlush(sicil);
            caris = CariResourceIT.createEntity(em);
        } else {
            caris = TestUtil.findAll(em, Cari.class).get(0);
        }
        em.persist(caris);
        em.flush();
        sicil.addCaris(caris);
        sicilRepository.saveAndFlush(sicil);
        Long carisId = caris.getId();

        // Get all the sicilList where caris equals to carisId
        defaultSicilShouldBeFound("carisId.equals=" + carisId);

        // Get all the sicilList where caris equals to (carisId + 1)
        defaultSicilShouldNotBeFound("carisId.equals=" + (carisId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSicilShouldBeFound(String filter) throws Exception {
        restSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sicil.getId().intValue())))
            .andExpect(jsonPath("$.[*].hesapno").value(hasItem(DEFAULT_HESAPNO)))
            .andExpect(jsonPath("$.[*].hesapadi").value(hasItem(DEFAULT_HESAPADI)))
            .andExpect(jsonPath("$.[*].adresi").value(hasItem(DEFAULT_ADRESI)))
            .andExpect(jsonPath("$.[*].ilce").value(hasItem(DEFAULT_ILCE)))
            .andExpect(jsonPath("$.[*].sehir").value(hasItem(DEFAULT_SEHIR)))
            .andExpect(jsonPath("$.[*].telefon").value(hasItem(DEFAULT_TELEFON)))
            .andExpect(jsonPath("$.[*].vda").value(hasItem(DEFAULT_VDA)))
            .andExpect(jsonPath("$.[*].vno").value(hasItem(DEFAULT_VNO)))
            .andExpect(jsonPath("$.[*].tcno").value(hasItem(DEFAULT_TCNO)))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].iskonto").value(hasItem(sameNumber(DEFAULT_ISKONTO))))
            .andExpect(jsonPath("$.[*].siciltipi").value(hasItem(DEFAULT_SICILTIPI.toString())));

        // Check, that the count call also returns 1
        restSicilMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSicilShouldNotBeFound(String filter) throws Exception {
        restSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSicilMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSicil() throws Exception {
        // Get the sicil
        restSicilMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSicil() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();

        // Update the sicil
        Sicil updatedSicil = sicilRepository.findById(sicil.getId()).get();
        // Disconnect from session so that the updates on updatedSicil are not directly saved in db
        em.detach(updatedSicil);
        updatedSicil
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .telefon(UPDATED_TELEFON)
            .vda(UPDATED_VDA)
            .vno(UPDATED_VNO)
            .tcno(UPDATED_TCNO)
            .gun(UPDATED_GUN)
            .iskonto(UPDATED_ISKONTO)
            .siciltipi(UPDATED_SICILTIPI);
        SicilDTO sicilDTO = sicilMapper.toDto(updatedSicil);

        restSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sicilDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(sicilDTO))
            )
            .andExpect(status().isOk());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
        Sicil testSicil = sicilList.get(sicilList.size() - 1);
        assertThat(testSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testSicil.getHesapadi()).isEqualTo(UPDATED_HESAPADI);
        assertThat(testSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testSicil.getIlce()).isEqualTo(UPDATED_ILCE);
        assertThat(testSicil.getSehir()).isEqualTo(UPDATED_SEHIR);
        assertThat(testSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testSicil.getVda()).isEqualTo(UPDATED_VDA);
        assertThat(testSicil.getVno()).isEqualTo(UPDATED_VNO);
        assertThat(testSicil.getTcno()).isEqualTo(UPDATED_TCNO);
        assertThat(testSicil.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testSicil.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testSicil.getSiciltipi()).isEqualTo(UPDATED_SICILTIPI);
    }

    @Test
    @Transactional
    void putNonExistingSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sicilDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(sicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(sicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(sicilDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSicilWithPatch() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();

        // Update the sicil using partial update
        Sicil partialUpdatedSicil = new Sicil();
        partialUpdatedSicil.setId(sicil.getId());

        partialUpdatedSicil
            .hesapno(UPDATED_HESAPNO)
            .adresi(UPDATED_ADRESI)
            .sehir(UPDATED_SEHIR)
            .telefon(UPDATED_TELEFON)
            .gun(UPDATED_GUN)
            .iskonto(UPDATED_ISKONTO);

        restSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSicil.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSicil))
            )
            .andExpect(status().isOk());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
        Sicil testSicil = sicilList.get(sicilList.size() - 1);
        assertThat(testSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testSicil.getHesapadi()).isEqualTo(DEFAULT_HESAPADI);
        assertThat(testSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testSicil.getIlce()).isEqualTo(DEFAULT_ILCE);
        assertThat(testSicil.getSehir()).isEqualTo(UPDATED_SEHIR);
        assertThat(testSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testSicil.getVda()).isEqualTo(DEFAULT_VDA);
        assertThat(testSicil.getVno()).isEqualTo(DEFAULT_VNO);
        assertThat(testSicil.getTcno()).isEqualTo(DEFAULT_TCNO);
        assertThat(testSicil.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testSicil.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testSicil.getSiciltipi()).isEqualTo(DEFAULT_SICILTIPI);
    }

    @Test
    @Transactional
    void fullUpdateSicilWithPatch() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();

        // Update the sicil using partial update
        Sicil partialUpdatedSicil = new Sicil();
        partialUpdatedSicil.setId(sicil.getId());

        partialUpdatedSicil
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .telefon(UPDATED_TELEFON)
            .vda(UPDATED_VDA)
            .vno(UPDATED_VNO)
            .tcno(UPDATED_TCNO)
            .gun(UPDATED_GUN)
            .iskonto(UPDATED_ISKONTO)
            .siciltipi(UPDATED_SICILTIPI);

        restSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSicil.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSicil))
            )
            .andExpect(status().isOk());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
        Sicil testSicil = sicilList.get(sicilList.size() - 1);
        assertThat(testSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testSicil.getHesapadi()).isEqualTo(UPDATED_HESAPADI);
        assertThat(testSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testSicil.getIlce()).isEqualTo(UPDATED_ILCE);
        assertThat(testSicil.getSehir()).isEqualTo(UPDATED_SEHIR);
        assertThat(testSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testSicil.getVda()).isEqualTo(UPDATED_VDA);
        assertThat(testSicil.getVno()).isEqualTo(UPDATED_VNO);
        assertThat(testSicil.getTcno()).isEqualTo(UPDATED_TCNO);
        assertThat(testSicil.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testSicil.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testSicil.getSiciltipi()).isEqualTo(UPDATED_SICILTIPI);
    }

    @Test
    @Transactional
    void patchNonExistingSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, sicilDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(sicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(sicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSicil() throws Exception {
        int databaseSizeBeforeUpdate = sicilRepository.findAll().size();
        sicil.setId(count.incrementAndGet());

        // Create the Sicil
        SicilDTO sicilDTO = sicilMapper.toDto(sicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSicilMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(sicilDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sicil in the database
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSicil() throws Exception {
        // Initialize the database
        sicilRepository.saveAndFlush(sicil);

        int databaseSizeBeforeDelete = sicilRepository.findAll().size();

        // Delete the sicil
        restSicilMockMvc
            .perform(delete(ENTITY_API_URL_ID, sicil.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Sicil> sicilList = sicilRepository.findAll();
        assertThat(sicilList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
