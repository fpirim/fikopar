package dev.fikopar.web.rest;

import static dev.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.fikopar.IntegrationTest;
import dev.fikopar.domain.Cari;
import dev.fikopar.domain.HareketDetay;
import dev.fikopar.domain.HareketSicil;
import dev.fikopar.domain.Sicil;
import dev.fikopar.domain.enumeration.DepoTipi;
import dev.fikopar.domain.enumeration.EvrakTipi;
import dev.fikopar.repository.HareketSicilRepository;
import dev.fikopar.service.criteria.HareketSicilCriteria;
import dev.fikopar.service.dto.HareketSicilDTO;
import dev.fikopar.service.mapper.HareketSicilMapper;
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
 * Integration tests for the {@link HareketSicilResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class HareketSicilResourceIT {

    private static final EvrakTipi DEFAULT_EVRAKTIPI = EvrakTipi.IRSALIYELI_SATIS_FATURASI;
    private static final EvrakTipi UPDATED_EVRAKTIPI = EvrakTipi.SATIS_FATURASI;

    private static final String DEFAULT_EVRAKNO = "AAAAAAAAAA";
    private static final String UPDATED_EVRAKNO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_TARIHI = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TARIHI = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_TARIHI = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_SEVKTARIHI = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SEVKTARIHI = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_SEVKTARIHI = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_SAATI = "AAAAAAAAAA";
    private static final String UPDATED_SAATI = "BBBBBBBBBB";

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

    private static final String DEFAULT_VDA = "AAAAAAAAAA";
    private static final String UPDATED_VDA = "BBBBBBBBBB";

    private static final String DEFAULT_TCNO = "AAAAAAAAAA";
    private static final String UPDATED_TCNO = "BBBBBBBBBB";

    private static final String DEFAULT_VNO = "AAAAAAAAAA";
    private static final String UPDATED_VNO = "BBBBBBBBBB";

    private static final String DEFAULT_TELEFON = "AAAAAAAAAA";
    private static final String UPDATED_TELEFON = "BBBBBBBBBB";

    private static final Integer DEFAULT_GUN = 1;
    private static final Integer UPDATED_GUN = 2;
    private static final Integer SMALLER_GUN = 1 - 1;

    private static final BigDecimal DEFAULT_TUTARI = new BigDecimal(1);
    private static final BigDecimal UPDATED_TUTARI = new BigDecimal(2);
    private static final BigDecimal SMALLER_TUTARI = new BigDecimal(1 - 1);

    private static final String DEFAULT_NOTLAR = "AAAAAAAAAA";
    private static final String UPDATED_NOTLAR = "BBBBBBBBBB";

    private static final DepoTipi DEFAULT_DEPOTIPI = DepoTipi.DEPO1;
    private static final DepoTipi UPDATED_DEPOTIPI = DepoTipi.DEPO2;

    private static final BigDecimal DEFAULT_TOPLAM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOPLAM = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOPLAM = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_ISKTOPLAM = new BigDecimal(1);
    private static final BigDecimal UPDATED_ISKTOPLAM = new BigDecimal(2);
    private static final BigDecimal SMALLER_ISKTOPLAM = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_KDVTOPLAM = new BigDecimal(1);
    private static final BigDecimal UPDATED_KDVTOPLAM = new BigDecimal(2);
    private static final BigDecimal SMALLER_KDVTOPLAM = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_ARATOPLAM = new BigDecimal(1);
    private static final BigDecimal UPDATED_ARATOPLAM = new BigDecimal(2);
    private static final BigDecimal SMALLER_ARATOPLAM = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/hareket-sicils";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private HareketSicilRepository hareketSicilRepository;

    @Autowired
    private HareketSicilMapper hareketSicilMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restHareketSicilMockMvc;

    private HareketSicil hareketSicil;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HareketSicil createEntity(EntityManager em) {
        HareketSicil hareketSicil = new HareketSicil()
            .evraktipi(DEFAULT_EVRAKTIPI)
            .evrakno(DEFAULT_EVRAKNO)
            .tarihi(DEFAULT_TARIHI)
            .sevktarihi(DEFAULT_SEVKTARIHI)
            .saati(DEFAULT_SAATI)
            .hesapno(DEFAULT_HESAPNO)
            .hesapadi(DEFAULT_HESAPADI)
            .adresi(DEFAULT_ADRESI)
            .ilce(DEFAULT_ILCE)
            .sehir(DEFAULT_SEHIR)
            .vda(DEFAULT_VDA)
            .tcno(DEFAULT_TCNO)
            .vno(DEFAULT_VNO)
            .telefon(DEFAULT_TELEFON)
            .gun(DEFAULT_GUN)
            .tutari(DEFAULT_TUTARI)
            .notlar(DEFAULT_NOTLAR)
            .depotipi(DEFAULT_DEPOTIPI)
            .toplam(DEFAULT_TOPLAM)
            .isktoplam(DEFAULT_ISKTOPLAM)
            .kdvtoplam(DEFAULT_KDVTOPLAM)
            .aratoplam(DEFAULT_ARATOPLAM);
        return hareketSicil;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HareketSicil createUpdatedEntity(EntityManager em) {
        HareketSicil hareketSicil = new HareketSicil()
            .evraktipi(UPDATED_EVRAKTIPI)
            .evrakno(UPDATED_EVRAKNO)
            .tarihi(UPDATED_TARIHI)
            .sevktarihi(UPDATED_SEVKTARIHI)
            .saati(UPDATED_SAATI)
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .vda(UPDATED_VDA)
            .tcno(UPDATED_TCNO)
            .vno(UPDATED_VNO)
            .telefon(UPDATED_TELEFON)
            .gun(UPDATED_GUN)
            .tutari(UPDATED_TUTARI)
            .notlar(UPDATED_NOTLAR)
            .depotipi(UPDATED_DEPOTIPI)
            .toplam(UPDATED_TOPLAM)
            .isktoplam(UPDATED_ISKTOPLAM)
            .kdvtoplam(UPDATED_KDVTOPLAM)
            .aratoplam(UPDATED_ARATOPLAM);
        return hareketSicil;
    }

    @BeforeEach
    public void initTest() {
        hareketSicil = createEntity(em);
    }

    @Test
    @Transactional
    void createHareketSicil() throws Exception {
        int databaseSizeBeforeCreate = hareketSicilRepository.findAll().size();
        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);
        restHareketSicilMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isCreated());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeCreate + 1);
        HareketSicil testHareketSicil = hareketSicilList.get(hareketSicilList.size() - 1);
        assertThat(testHareketSicil.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testHareketSicil.getEvrakno()).isEqualTo(DEFAULT_EVRAKNO);
        assertThat(testHareketSicil.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testHareketSicil.getSevktarihi()).isEqualTo(DEFAULT_SEVKTARIHI);
        assertThat(testHareketSicil.getSaati()).isEqualTo(DEFAULT_SAATI);
        assertThat(testHareketSicil.getHesapno()).isEqualTo(DEFAULT_HESAPNO);
        assertThat(testHareketSicil.getHesapadi()).isEqualTo(DEFAULT_HESAPADI);
        assertThat(testHareketSicil.getAdresi()).isEqualTo(DEFAULT_ADRESI);
        assertThat(testHareketSicil.getIlce()).isEqualTo(DEFAULT_ILCE);
        assertThat(testHareketSicil.getSehir()).isEqualTo(DEFAULT_SEHIR);
        assertThat(testHareketSicil.getVda()).isEqualTo(DEFAULT_VDA);
        assertThat(testHareketSicil.getTcno()).isEqualTo(DEFAULT_TCNO);
        assertThat(testHareketSicil.getVno()).isEqualTo(DEFAULT_VNO);
        assertThat(testHareketSicil.getTelefon()).isEqualTo(DEFAULT_TELEFON);
        assertThat(testHareketSicil.getGun()).isEqualTo(DEFAULT_GUN);
        assertThat(testHareketSicil.getTutari()).isEqualByComparingTo(DEFAULT_TUTARI);
        assertThat(testHareketSicil.getNotlar()).isEqualTo(DEFAULT_NOTLAR);
        assertThat(testHareketSicil.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testHareketSicil.getToplam()).isEqualByComparingTo(DEFAULT_TOPLAM);
        assertThat(testHareketSicil.getIsktoplam()).isEqualByComparingTo(DEFAULT_ISKTOPLAM);
        assertThat(testHareketSicil.getKdvtoplam()).isEqualByComparingTo(DEFAULT_KDVTOPLAM);
        assertThat(testHareketSicil.getAratoplam()).isEqualByComparingTo(DEFAULT_ARATOPLAM);
    }

    @Test
    @Transactional
    void createHareketSicilWithExistingId() throws Exception {
        // Create the HareketSicil with an existing ID
        hareketSicil.setId(1L);
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        int databaseSizeBeforeCreate = hareketSicilRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restHareketSicilMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllHareketSicils() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hareketSicil.getId().intValue())))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].evrakno").value(hasItem(DEFAULT_EVRAKNO)))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].sevktarihi").value(hasItem(DEFAULT_SEVKTARIHI.toString())))
            .andExpect(jsonPath("$.[*].saati").value(hasItem(DEFAULT_SAATI)))
            .andExpect(jsonPath("$.[*].hesapno").value(hasItem(DEFAULT_HESAPNO)))
            .andExpect(jsonPath("$.[*].hesapadi").value(hasItem(DEFAULT_HESAPADI)))
            .andExpect(jsonPath("$.[*].adresi").value(hasItem(DEFAULT_ADRESI)))
            .andExpect(jsonPath("$.[*].ilce").value(hasItem(DEFAULT_ILCE)))
            .andExpect(jsonPath("$.[*].sehir").value(hasItem(DEFAULT_SEHIR)))
            .andExpect(jsonPath("$.[*].vda").value(hasItem(DEFAULT_VDA)))
            .andExpect(jsonPath("$.[*].tcno").value(hasItem(DEFAULT_TCNO)))
            .andExpect(jsonPath("$.[*].vno").value(hasItem(DEFAULT_VNO)))
            .andExpect(jsonPath("$.[*].telefon").value(hasItem(DEFAULT_TELEFON)))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].tutari").value(hasItem(sameNumber(DEFAULT_TUTARI))))
            .andExpect(jsonPath("$.[*].notlar").value(hasItem(DEFAULT_NOTLAR)))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].toplam").value(hasItem(sameNumber(DEFAULT_TOPLAM))))
            .andExpect(jsonPath("$.[*].isktoplam").value(hasItem(sameNumber(DEFAULT_ISKTOPLAM))))
            .andExpect(jsonPath("$.[*].kdvtoplam").value(hasItem(sameNumber(DEFAULT_KDVTOPLAM))))
            .andExpect(jsonPath("$.[*].aratoplam").value(hasItem(sameNumber(DEFAULT_ARATOPLAM))));
    }

    @Test
    @Transactional
    void getHareketSicil() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get the hareketSicil
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL_ID, hareketSicil.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(hareketSicil.getId().intValue()))
            .andExpect(jsonPath("$.evraktipi").value(DEFAULT_EVRAKTIPI.toString()))
            .andExpect(jsonPath("$.evrakno").value(DEFAULT_EVRAKNO))
            .andExpect(jsonPath("$.tarihi").value(DEFAULT_TARIHI.toString()))
            .andExpect(jsonPath("$.sevktarihi").value(DEFAULT_SEVKTARIHI.toString()))
            .andExpect(jsonPath("$.saati").value(DEFAULT_SAATI))
            .andExpect(jsonPath("$.hesapno").value(DEFAULT_HESAPNO))
            .andExpect(jsonPath("$.hesapadi").value(DEFAULT_HESAPADI))
            .andExpect(jsonPath("$.adresi").value(DEFAULT_ADRESI))
            .andExpect(jsonPath("$.ilce").value(DEFAULT_ILCE))
            .andExpect(jsonPath("$.sehir").value(DEFAULT_SEHIR))
            .andExpect(jsonPath("$.vda").value(DEFAULT_VDA))
            .andExpect(jsonPath("$.tcno").value(DEFAULT_TCNO))
            .andExpect(jsonPath("$.vno").value(DEFAULT_VNO))
            .andExpect(jsonPath("$.telefon").value(DEFAULT_TELEFON))
            .andExpect(jsonPath("$.gun").value(DEFAULT_GUN))
            .andExpect(jsonPath("$.tutari").value(sameNumber(DEFAULT_TUTARI)))
            .andExpect(jsonPath("$.notlar").value(DEFAULT_NOTLAR))
            .andExpect(jsonPath("$.depotipi").value(DEFAULT_DEPOTIPI.toString()))
            .andExpect(jsonPath("$.toplam").value(sameNumber(DEFAULT_TOPLAM)))
            .andExpect(jsonPath("$.isktoplam").value(sameNumber(DEFAULT_ISKTOPLAM)))
            .andExpect(jsonPath("$.kdvtoplam").value(sameNumber(DEFAULT_KDVTOPLAM)))
            .andExpect(jsonPath("$.aratoplam").value(sameNumber(DEFAULT_ARATOPLAM)));
    }

    @Test
    @Transactional
    void getHareketSicilsByIdFiltering() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        Long id = hareketSicil.getId();

        defaultHareketSicilShouldBeFound("id.equals=" + id);
        defaultHareketSicilShouldNotBeFound("id.notEquals=" + id);

        defaultHareketSicilShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultHareketSicilShouldNotBeFound("id.greaterThan=" + id);

        defaultHareketSicilShouldBeFound("id.lessThanOrEqual=" + id);
        defaultHareketSicilShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraktipiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evraktipi equals to DEFAULT_EVRAKTIPI
        defaultHareketSicilShouldBeFound("evraktipi.equals=" + DEFAULT_EVRAKTIPI);

        // Get all the hareketSicilList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultHareketSicilShouldNotBeFound("evraktipi.equals=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraktipiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evraktipi in DEFAULT_EVRAKTIPI or UPDATED_EVRAKTIPI
        defaultHareketSicilShouldBeFound("evraktipi.in=" + DEFAULT_EVRAKTIPI + "," + UPDATED_EVRAKTIPI);

        // Get all the hareketSicilList where evraktipi equals to UPDATED_EVRAKTIPI
        defaultHareketSicilShouldNotBeFound("evraktipi.in=" + UPDATED_EVRAKTIPI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraktipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evraktipi is not null
        defaultHareketSicilShouldBeFound("evraktipi.specified=true");

        // Get all the hareketSicilList where evraktipi is null
        defaultHareketSicilShouldNotBeFound("evraktipi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraknoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evrakno equals to DEFAULT_EVRAKNO
        defaultHareketSicilShouldBeFound("evrakno.equals=" + DEFAULT_EVRAKNO);

        // Get all the hareketSicilList where evrakno equals to UPDATED_EVRAKNO
        defaultHareketSicilShouldNotBeFound("evrakno.equals=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraknoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evrakno in DEFAULT_EVRAKNO or UPDATED_EVRAKNO
        defaultHareketSicilShouldBeFound("evrakno.in=" + DEFAULT_EVRAKNO + "," + UPDATED_EVRAKNO);

        // Get all the hareketSicilList where evrakno equals to UPDATED_EVRAKNO
        defaultHareketSicilShouldNotBeFound("evrakno.in=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraknoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evrakno is not null
        defaultHareketSicilShouldBeFound("evrakno.specified=true");

        // Get all the hareketSicilList where evrakno is null
        defaultHareketSicilShouldNotBeFound("evrakno.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraknoContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evrakno contains DEFAULT_EVRAKNO
        defaultHareketSicilShouldBeFound("evrakno.contains=" + DEFAULT_EVRAKNO);

        // Get all the hareketSicilList where evrakno contains UPDATED_EVRAKNO
        defaultHareketSicilShouldNotBeFound("evrakno.contains=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByEvraknoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where evrakno does not contain DEFAULT_EVRAKNO
        defaultHareketSicilShouldNotBeFound("evrakno.doesNotContain=" + DEFAULT_EVRAKNO);

        // Get all the hareketSicilList where evrakno does not contain UPDATED_EVRAKNO
        defaultHareketSicilShouldBeFound("evrakno.doesNotContain=" + UPDATED_EVRAKNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi equals to DEFAULT_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.equals=" + DEFAULT_TARIHI);

        // Get all the hareketSicilList where tarihi equals to UPDATED_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.equals=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi in DEFAULT_TARIHI or UPDATED_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.in=" + DEFAULT_TARIHI + "," + UPDATED_TARIHI);

        // Get all the hareketSicilList where tarihi equals to UPDATED_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.in=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi is not null
        defaultHareketSicilShouldBeFound("tarihi.specified=true");

        // Get all the hareketSicilList where tarihi is null
        defaultHareketSicilShouldNotBeFound("tarihi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi is greater than or equal to DEFAULT_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.greaterThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the hareketSicilList where tarihi is greater than or equal to UPDATED_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.greaterThanOrEqual=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi is less than or equal to DEFAULT_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.lessThanOrEqual=" + DEFAULT_TARIHI);

        // Get all the hareketSicilList where tarihi is less than or equal to SMALLER_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.lessThanOrEqual=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi is less than DEFAULT_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.lessThan=" + DEFAULT_TARIHI);

        // Get all the hareketSicilList where tarihi is less than UPDATED_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.lessThan=" + UPDATED_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTarihiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tarihi is greater than DEFAULT_TARIHI
        defaultHareketSicilShouldNotBeFound("tarihi.greaterThan=" + DEFAULT_TARIHI);

        // Get all the hareketSicilList where tarihi is greater than SMALLER_TARIHI
        defaultHareketSicilShouldBeFound("tarihi.greaterThan=" + SMALLER_TARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi equals to DEFAULT_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.equals=" + DEFAULT_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi equals to UPDATED_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.equals=" + UPDATED_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi in DEFAULT_SEVKTARIHI or UPDATED_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.in=" + DEFAULT_SEVKTARIHI + "," + UPDATED_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi equals to UPDATED_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.in=" + UPDATED_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi is not null
        defaultHareketSicilShouldBeFound("sevktarihi.specified=true");

        // Get all the hareketSicilList where sevktarihi is null
        defaultHareketSicilShouldNotBeFound("sevktarihi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi is greater than or equal to DEFAULT_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.greaterThanOrEqual=" + DEFAULT_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi is greater than or equal to UPDATED_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.greaterThanOrEqual=" + UPDATED_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi is less than or equal to DEFAULT_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.lessThanOrEqual=" + DEFAULT_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi is less than or equal to SMALLER_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.lessThanOrEqual=" + SMALLER_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi is less than DEFAULT_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.lessThan=" + DEFAULT_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi is less than UPDATED_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.lessThan=" + UPDATED_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySevktarihiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sevktarihi is greater than DEFAULT_SEVKTARIHI
        defaultHareketSicilShouldNotBeFound("sevktarihi.greaterThan=" + DEFAULT_SEVKTARIHI);

        // Get all the hareketSicilList where sevktarihi is greater than SMALLER_SEVKTARIHI
        defaultHareketSicilShouldBeFound("sevktarihi.greaterThan=" + SMALLER_SEVKTARIHI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySaatiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where saati equals to DEFAULT_SAATI
        defaultHareketSicilShouldBeFound("saati.equals=" + DEFAULT_SAATI);

        // Get all the hareketSicilList where saati equals to UPDATED_SAATI
        defaultHareketSicilShouldNotBeFound("saati.equals=" + UPDATED_SAATI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySaatiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where saati in DEFAULT_SAATI or UPDATED_SAATI
        defaultHareketSicilShouldBeFound("saati.in=" + DEFAULT_SAATI + "," + UPDATED_SAATI);

        // Get all the hareketSicilList where saati equals to UPDATED_SAATI
        defaultHareketSicilShouldNotBeFound("saati.in=" + UPDATED_SAATI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySaatiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where saati is not null
        defaultHareketSicilShouldBeFound("saati.specified=true");

        // Get all the hareketSicilList where saati is null
        defaultHareketSicilShouldNotBeFound("saati.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySaatiContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where saati contains DEFAULT_SAATI
        defaultHareketSicilShouldBeFound("saati.contains=" + DEFAULT_SAATI);

        // Get all the hareketSicilList where saati contains UPDATED_SAATI
        defaultHareketSicilShouldNotBeFound("saati.contains=" + UPDATED_SAATI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySaatiNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where saati does not contain DEFAULT_SAATI
        defaultHareketSicilShouldNotBeFound("saati.doesNotContain=" + DEFAULT_SAATI);

        // Get all the hareketSicilList where saati does not contain UPDATED_SAATI
        defaultHareketSicilShouldBeFound("saati.doesNotContain=" + UPDATED_SAATI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapnoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapno equals to DEFAULT_HESAPNO
        defaultHareketSicilShouldBeFound("hesapno.equals=" + DEFAULT_HESAPNO);

        // Get all the hareketSicilList where hesapno equals to UPDATED_HESAPNO
        defaultHareketSicilShouldNotBeFound("hesapno.equals=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapnoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapno in DEFAULT_HESAPNO or UPDATED_HESAPNO
        defaultHareketSicilShouldBeFound("hesapno.in=" + DEFAULT_HESAPNO + "," + UPDATED_HESAPNO);

        // Get all the hareketSicilList where hesapno equals to UPDATED_HESAPNO
        defaultHareketSicilShouldNotBeFound("hesapno.in=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapno is not null
        defaultHareketSicilShouldBeFound("hesapno.specified=true");

        // Get all the hareketSicilList where hesapno is null
        defaultHareketSicilShouldNotBeFound("hesapno.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapnoContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapno contains DEFAULT_HESAPNO
        defaultHareketSicilShouldBeFound("hesapno.contains=" + DEFAULT_HESAPNO);

        // Get all the hareketSicilList where hesapno contains UPDATED_HESAPNO
        defaultHareketSicilShouldNotBeFound("hesapno.contains=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapnoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapno does not contain DEFAULT_HESAPNO
        defaultHareketSicilShouldNotBeFound("hesapno.doesNotContain=" + DEFAULT_HESAPNO);

        // Get all the hareketSicilList where hesapno does not contain UPDATED_HESAPNO
        defaultHareketSicilShouldBeFound("hesapno.doesNotContain=" + UPDATED_HESAPNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapadiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapadi equals to DEFAULT_HESAPADI
        defaultHareketSicilShouldBeFound("hesapadi.equals=" + DEFAULT_HESAPADI);

        // Get all the hareketSicilList where hesapadi equals to UPDATED_HESAPADI
        defaultHareketSicilShouldNotBeFound("hesapadi.equals=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapadiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapadi in DEFAULT_HESAPADI or UPDATED_HESAPADI
        defaultHareketSicilShouldBeFound("hesapadi.in=" + DEFAULT_HESAPADI + "," + UPDATED_HESAPADI);

        // Get all the hareketSicilList where hesapadi equals to UPDATED_HESAPADI
        defaultHareketSicilShouldNotBeFound("hesapadi.in=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapadiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapadi is not null
        defaultHareketSicilShouldBeFound("hesapadi.specified=true");

        // Get all the hareketSicilList where hesapadi is null
        defaultHareketSicilShouldNotBeFound("hesapadi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapadiContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapadi contains DEFAULT_HESAPADI
        defaultHareketSicilShouldBeFound("hesapadi.contains=" + DEFAULT_HESAPADI);

        // Get all the hareketSicilList where hesapadi contains UPDATED_HESAPADI
        defaultHareketSicilShouldNotBeFound("hesapadi.contains=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHesapadiNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where hesapadi does not contain DEFAULT_HESAPADI
        defaultHareketSicilShouldNotBeFound("hesapadi.doesNotContain=" + DEFAULT_HESAPADI);

        // Get all the hareketSicilList where hesapadi does not contain UPDATED_HESAPADI
        defaultHareketSicilShouldBeFound("hesapadi.doesNotContain=" + UPDATED_HESAPADI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAdresiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where adresi equals to DEFAULT_ADRESI
        defaultHareketSicilShouldBeFound("adresi.equals=" + DEFAULT_ADRESI);

        // Get all the hareketSicilList where adresi equals to UPDATED_ADRESI
        defaultHareketSicilShouldNotBeFound("adresi.equals=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAdresiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where adresi in DEFAULT_ADRESI or UPDATED_ADRESI
        defaultHareketSicilShouldBeFound("adresi.in=" + DEFAULT_ADRESI + "," + UPDATED_ADRESI);

        // Get all the hareketSicilList where adresi equals to UPDATED_ADRESI
        defaultHareketSicilShouldNotBeFound("adresi.in=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAdresiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where adresi is not null
        defaultHareketSicilShouldBeFound("adresi.specified=true");

        // Get all the hareketSicilList where adresi is null
        defaultHareketSicilShouldNotBeFound("adresi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAdresiContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where adresi contains DEFAULT_ADRESI
        defaultHareketSicilShouldBeFound("adresi.contains=" + DEFAULT_ADRESI);

        // Get all the hareketSicilList where adresi contains UPDATED_ADRESI
        defaultHareketSicilShouldNotBeFound("adresi.contains=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAdresiNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where adresi does not contain DEFAULT_ADRESI
        defaultHareketSicilShouldNotBeFound("adresi.doesNotContain=" + DEFAULT_ADRESI);

        // Get all the hareketSicilList where adresi does not contain UPDATED_ADRESI
        defaultHareketSicilShouldBeFound("adresi.doesNotContain=" + UPDATED_ADRESI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIlceIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where ilce equals to DEFAULT_ILCE
        defaultHareketSicilShouldBeFound("ilce.equals=" + DEFAULT_ILCE);

        // Get all the hareketSicilList where ilce equals to UPDATED_ILCE
        defaultHareketSicilShouldNotBeFound("ilce.equals=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIlceIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where ilce in DEFAULT_ILCE or UPDATED_ILCE
        defaultHareketSicilShouldBeFound("ilce.in=" + DEFAULT_ILCE + "," + UPDATED_ILCE);

        // Get all the hareketSicilList where ilce equals to UPDATED_ILCE
        defaultHareketSicilShouldNotBeFound("ilce.in=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIlceIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where ilce is not null
        defaultHareketSicilShouldBeFound("ilce.specified=true");

        // Get all the hareketSicilList where ilce is null
        defaultHareketSicilShouldNotBeFound("ilce.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIlceContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where ilce contains DEFAULT_ILCE
        defaultHareketSicilShouldBeFound("ilce.contains=" + DEFAULT_ILCE);

        // Get all the hareketSicilList where ilce contains UPDATED_ILCE
        defaultHareketSicilShouldNotBeFound("ilce.contains=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIlceNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where ilce does not contain DEFAULT_ILCE
        defaultHareketSicilShouldNotBeFound("ilce.doesNotContain=" + DEFAULT_ILCE);

        // Get all the hareketSicilList where ilce does not contain UPDATED_ILCE
        defaultHareketSicilShouldBeFound("ilce.doesNotContain=" + UPDATED_ILCE);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySehirIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sehir equals to DEFAULT_SEHIR
        defaultHareketSicilShouldBeFound("sehir.equals=" + DEFAULT_SEHIR);

        // Get all the hareketSicilList where sehir equals to UPDATED_SEHIR
        defaultHareketSicilShouldNotBeFound("sehir.equals=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySehirIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sehir in DEFAULT_SEHIR or UPDATED_SEHIR
        defaultHareketSicilShouldBeFound("sehir.in=" + DEFAULT_SEHIR + "," + UPDATED_SEHIR);

        // Get all the hareketSicilList where sehir equals to UPDATED_SEHIR
        defaultHareketSicilShouldNotBeFound("sehir.in=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySehirIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sehir is not null
        defaultHareketSicilShouldBeFound("sehir.specified=true");

        // Get all the hareketSicilList where sehir is null
        defaultHareketSicilShouldNotBeFound("sehir.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySehirContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sehir contains DEFAULT_SEHIR
        defaultHareketSicilShouldBeFound("sehir.contains=" + DEFAULT_SEHIR);

        // Get all the hareketSicilList where sehir contains UPDATED_SEHIR
        defaultHareketSicilShouldNotBeFound("sehir.contains=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySehirNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where sehir does not contain DEFAULT_SEHIR
        defaultHareketSicilShouldNotBeFound("sehir.doesNotContain=" + DEFAULT_SEHIR);

        // Get all the hareketSicilList where sehir does not contain UPDATED_SEHIR
        defaultHareketSicilShouldBeFound("sehir.doesNotContain=" + UPDATED_SEHIR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVdaIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vda equals to DEFAULT_VDA
        defaultHareketSicilShouldBeFound("vda.equals=" + DEFAULT_VDA);

        // Get all the hareketSicilList where vda equals to UPDATED_VDA
        defaultHareketSicilShouldNotBeFound("vda.equals=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVdaIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vda in DEFAULT_VDA or UPDATED_VDA
        defaultHareketSicilShouldBeFound("vda.in=" + DEFAULT_VDA + "," + UPDATED_VDA);

        // Get all the hareketSicilList where vda equals to UPDATED_VDA
        defaultHareketSicilShouldNotBeFound("vda.in=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVdaIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vda is not null
        defaultHareketSicilShouldBeFound("vda.specified=true");

        // Get all the hareketSicilList where vda is null
        defaultHareketSicilShouldNotBeFound("vda.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVdaContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vda contains DEFAULT_VDA
        defaultHareketSicilShouldBeFound("vda.contains=" + DEFAULT_VDA);

        // Get all the hareketSicilList where vda contains UPDATED_VDA
        defaultHareketSicilShouldNotBeFound("vda.contains=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVdaNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vda does not contain DEFAULT_VDA
        defaultHareketSicilShouldNotBeFound("vda.doesNotContain=" + DEFAULT_VDA);

        // Get all the hareketSicilList where vda does not contain UPDATED_VDA
        defaultHareketSicilShouldBeFound("vda.doesNotContain=" + UPDATED_VDA);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTcnoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tcno equals to DEFAULT_TCNO
        defaultHareketSicilShouldBeFound("tcno.equals=" + DEFAULT_TCNO);

        // Get all the hareketSicilList where tcno equals to UPDATED_TCNO
        defaultHareketSicilShouldNotBeFound("tcno.equals=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTcnoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tcno in DEFAULT_TCNO or UPDATED_TCNO
        defaultHareketSicilShouldBeFound("tcno.in=" + DEFAULT_TCNO + "," + UPDATED_TCNO);

        // Get all the hareketSicilList where tcno equals to UPDATED_TCNO
        defaultHareketSicilShouldNotBeFound("tcno.in=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTcnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tcno is not null
        defaultHareketSicilShouldBeFound("tcno.specified=true");

        // Get all the hareketSicilList where tcno is null
        defaultHareketSicilShouldNotBeFound("tcno.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTcnoContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tcno contains DEFAULT_TCNO
        defaultHareketSicilShouldBeFound("tcno.contains=" + DEFAULT_TCNO);

        // Get all the hareketSicilList where tcno contains UPDATED_TCNO
        defaultHareketSicilShouldNotBeFound("tcno.contains=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTcnoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tcno does not contain DEFAULT_TCNO
        defaultHareketSicilShouldNotBeFound("tcno.doesNotContain=" + DEFAULT_TCNO);

        // Get all the hareketSicilList where tcno does not contain UPDATED_TCNO
        defaultHareketSicilShouldBeFound("tcno.doesNotContain=" + UPDATED_TCNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVnoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vno equals to DEFAULT_VNO
        defaultHareketSicilShouldBeFound("vno.equals=" + DEFAULT_VNO);

        // Get all the hareketSicilList where vno equals to UPDATED_VNO
        defaultHareketSicilShouldNotBeFound("vno.equals=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVnoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vno in DEFAULT_VNO or UPDATED_VNO
        defaultHareketSicilShouldBeFound("vno.in=" + DEFAULT_VNO + "," + UPDATED_VNO);

        // Get all the hareketSicilList where vno equals to UPDATED_VNO
        defaultHareketSicilShouldNotBeFound("vno.in=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVnoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vno is not null
        defaultHareketSicilShouldBeFound("vno.specified=true");

        // Get all the hareketSicilList where vno is null
        defaultHareketSicilShouldNotBeFound("vno.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVnoContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vno contains DEFAULT_VNO
        defaultHareketSicilShouldBeFound("vno.contains=" + DEFAULT_VNO);

        // Get all the hareketSicilList where vno contains UPDATED_VNO
        defaultHareketSicilShouldNotBeFound("vno.contains=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByVnoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where vno does not contain DEFAULT_VNO
        defaultHareketSicilShouldNotBeFound("vno.doesNotContain=" + DEFAULT_VNO);

        // Get all the hareketSicilList where vno does not contain UPDATED_VNO
        defaultHareketSicilShouldBeFound("vno.doesNotContain=" + UPDATED_VNO);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTelefonIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where telefon equals to DEFAULT_TELEFON
        defaultHareketSicilShouldBeFound("telefon.equals=" + DEFAULT_TELEFON);

        // Get all the hareketSicilList where telefon equals to UPDATED_TELEFON
        defaultHareketSicilShouldNotBeFound("telefon.equals=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTelefonIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where telefon in DEFAULT_TELEFON or UPDATED_TELEFON
        defaultHareketSicilShouldBeFound("telefon.in=" + DEFAULT_TELEFON + "," + UPDATED_TELEFON);

        // Get all the hareketSicilList where telefon equals to UPDATED_TELEFON
        defaultHareketSicilShouldNotBeFound("telefon.in=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTelefonIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where telefon is not null
        defaultHareketSicilShouldBeFound("telefon.specified=true");

        // Get all the hareketSicilList where telefon is null
        defaultHareketSicilShouldNotBeFound("telefon.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTelefonContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where telefon contains DEFAULT_TELEFON
        defaultHareketSicilShouldBeFound("telefon.contains=" + DEFAULT_TELEFON);

        // Get all the hareketSicilList where telefon contains UPDATED_TELEFON
        defaultHareketSicilShouldNotBeFound("telefon.contains=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTelefonNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where telefon does not contain DEFAULT_TELEFON
        defaultHareketSicilShouldNotBeFound("telefon.doesNotContain=" + DEFAULT_TELEFON);

        // Get all the hareketSicilList where telefon does not contain UPDATED_TELEFON
        defaultHareketSicilShouldBeFound("telefon.doesNotContain=" + UPDATED_TELEFON);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun equals to DEFAULT_GUN
        defaultHareketSicilShouldBeFound("gun.equals=" + DEFAULT_GUN);

        // Get all the hareketSicilList where gun equals to UPDATED_GUN
        defaultHareketSicilShouldNotBeFound("gun.equals=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun in DEFAULT_GUN or UPDATED_GUN
        defaultHareketSicilShouldBeFound("gun.in=" + DEFAULT_GUN + "," + UPDATED_GUN);

        // Get all the hareketSicilList where gun equals to UPDATED_GUN
        defaultHareketSicilShouldNotBeFound("gun.in=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun is not null
        defaultHareketSicilShouldBeFound("gun.specified=true");

        // Get all the hareketSicilList where gun is null
        defaultHareketSicilShouldNotBeFound("gun.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun is greater than or equal to DEFAULT_GUN
        defaultHareketSicilShouldBeFound("gun.greaterThanOrEqual=" + DEFAULT_GUN);

        // Get all the hareketSicilList where gun is greater than or equal to UPDATED_GUN
        defaultHareketSicilShouldNotBeFound("gun.greaterThanOrEqual=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun is less than or equal to DEFAULT_GUN
        defaultHareketSicilShouldBeFound("gun.lessThanOrEqual=" + DEFAULT_GUN);

        // Get all the hareketSicilList where gun is less than or equal to SMALLER_GUN
        defaultHareketSicilShouldNotBeFound("gun.lessThanOrEqual=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun is less than DEFAULT_GUN
        defaultHareketSicilShouldNotBeFound("gun.lessThan=" + DEFAULT_GUN);

        // Get all the hareketSicilList where gun is less than UPDATED_GUN
        defaultHareketSicilShouldBeFound("gun.lessThan=" + UPDATED_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByGunIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where gun is greater than DEFAULT_GUN
        defaultHareketSicilShouldNotBeFound("gun.greaterThan=" + DEFAULT_GUN);

        // Get all the hareketSicilList where gun is greater than SMALLER_GUN
        defaultHareketSicilShouldBeFound("gun.greaterThan=" + SMALLER_GUN);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari equals to DEFAULT_TUTARI
        defaultHareketSicilShouldBeFound("tutari.equals=" + DEFAULT_TUTARI);

        // Get all the hareketSicilList where tutari equals to UPDATED_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.equals=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari in DEFAULT_TUTARI or UPDATED_TUTARI
        defaultHareketSicilShouldBeFound("tutari.in=" + DEFAULT_TUTARI + "," + UPDATED_TUTARI);

        // Get all the hareketSicilList where tutari equals to UPDATED_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.in=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari is not null
        defaultHareketSicilShouldBeFound("tutari.specified=true");

        // Get all the hareketSicilList where tutari is null
        defaultHareketSicilShouldNotBeFound("tutari.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari is greater than or equal to DEFAULT_TUTARI
        defaultHareketSicilShouldBeFound("tutari.greaterThanOrEqual=" + DEFAULT_TUTARI);

        // Get all the hareketSicilList where tutari is greater than or equal to UPDATED_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.greaterThanOrEqual=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari is less than or equal to DEFAULT_TUTARI
        defaultHareketSicilShouldBeFound("tutari.lessThanOrEqual=" + DEFAULT_TUTARI);

        // Get all the hareketSicilList where tutari is less than or equal to SMALLER_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.lessThanOrEqual=" + SMALLER_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari is less than DEFAULT_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.lessThan=" + DEFAULT_TUTARI);

        // Get all the hareketSicilList where tutari is less than UPDATED_TUTARI
        defaultHareketSicilShouldBeFound("tutari.lessThan=" + UPDATED_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByTutariIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where tutari is greater than DEFAULT_TUTARI
        defaultHareketSicilShouldNotBeFound("tutari.greaterThan=" + DEFAULT_TUTARI);

        // Get all the hareketSicilList where tutari is greater than SMALLER_TUTARI
        defaultHareketSicilShouldBeFound("tutari.greaterThan=" + SMALLER_TUTARI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByNotlarIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where notlar equals to DEFAULT_NOTLAR
        defaultHareketSicilShouldBeFound("notlar.equals=" + DEFAULT_NOTLAR);

        // Get all the hareketSicilList where notlar equals to UPDATED_NOTLAR
        defaultHareketSicilShouldNotBeFound("notlar.equals=" + UPDATED_NOTLAR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByNotlarIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where notlar in DEFAULT_NOTLAR or UPDATED_NOTLAR
        defaultHareketSicilShouldBeFound("notlar.in=" + DEFAULT_NOTLAR + "," + UPDATED_NOTLAR);

        // Get all the hareketSicilList where notlar equals to UPDATED_NOTLAR
        defaultHareketSicilShouldNotBeFound("notlar.in=" + UPDATED_NOTLAR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByNotlarIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where notlar is not null
        defaultHareketSicilShouldBeFound("notlar.specified=true");

        // Get all the hareketSicilList where notlar is null
        defaultHareketSicilShouldNotBeFound("notlar.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByNotlarContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where notlar contains DEFAULT_NOTLAR
        defaultHareketSicilShouldBeFound("notlar.contains=" + DEFAULT_NOTLAR);

        // Get all the hareketSicilList where notlar contains UPDATED_NOTLAR
        defaultHareketSicilShouldNotBeFound("notlar.contains=" + UPDATED_NOTLAR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByNotlarNotContainsSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where notlar does not contain DEFAULT_NOTLAR
        defaultHareketSicilShouldNotBeFound("notlar.doesNotContain=" + DEFAULT_NOTLAR);

        // Get all the hareketSicilList where notlar does not contain UPDATED_NOTLAR
        defaultHareketSicilShouldBeFound("notlar.doesNotContain=" + UPDATED_NOTLAR);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByDepotipiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where depotipi equals to DEFAULT_DEPOTIPI
        defaultHareketSicilShouldBeFound("depotipi.equals=" + DEFAULT_DEPOTIPI);

        // Get all the hareketSicilList where depotipi equals to UPDATED_DEPOTIPI
        defaultHareketSicilShouldNotBeFound("depotipi.equals=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByDepotipiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where depotipi in DEFAULT_DEPOTIPI or UPDATED_DEPOTIPI
        defaultHareketSicilShouldBeFound("depotipi.in=" + DEFAULT_DEPOTIPI + "," + UPDATED_DEPOTIPI);

        // Get all the hareketSicilList where depotipi equals to UPDATED_DEPOTIPI
        defaultHareketSicilShouldNotBeFound("depotipi.in=" + UPDATED_DEPOTIPI);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByDepotipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where depotipi is not null
        defaultHareketSicilShouldBeFound("depotipi.specified=true");

        // Get all the hareketSicilList where depotipi is null
        defaultHareketSicilShouldNotBeFound("depotipi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam equals to DEFAULT_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.equals=" + DEFAULT_TOPLAM);

        // Get all the hareketSicilList where toplam equals to UPDATED_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.equals=" + UPDATED_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam in DEFAULT_TOPLAM or UPDATED_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.in=" + DEFAULT_TOPLAM + "," + UPDATED_TOPLAM);

        // Get all the hareketSicilList where toplam equals to UPDATED_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.in=" + UPDATED_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam is not null
        defaultHareketSicilShouldBeFound("toplam.specified=true");

        // Get all the hareketSicilList where toplam is null
        defaultHareketSicilShouldNotBeFound("toplam.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam is greater than or equal to DEFAULT_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.greaterThanOrEqual=" + DEFAULT_TOPLAM);

        // Get all the hareketSicilList where toplam is greater than or equal to UPDATED_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.greaterThanOrEqual=" + UPDATED_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam is less than or equal to DEFAULT_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.lessThanOrEqual=" + DEFAULT_TOPLAM);

        // Get all the hareketSicilList where toplam is less than or equal to SMALLER_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.lessThanOrEqual=" + SMALLER_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam is less than DEFAULT_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.lessThan=" + DEFAULT_TOPLAM);

        // Get all the hareketSicilList where toplam is less than UPDATED_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.lessThan=" + UPDATED_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByToplamIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where toplam is greater than DEFAULT_TOPLAM
        defaultHareketSicilShouldNotBeFound("toplam.greaterThan=" + DEFAULT_TOPLAM);

        // Get all the hareketSicilList where toplam is greater than SMALLER_TOPLAM
        defaultHareketSicilShouldBeFound("toplam.greaterThan=" + SMALLER_TOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam equals to DEFAULT_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.equals=" + DEFAULT_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam equals to UPDATED_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.equals=" + UPDATED_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam in DEFAULT_ISKTOPLAM or UPDATED_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.in=" + DEFAULT_ISKTOPLAM + "," + UPDATED_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam equals to UPDATED_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.in=" + UPDATED_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam is not null
        defaultHareketSicilShouldBeFound("isktoplam.specified=true");

        // Get all the hareketSicilList where isktoplam is null
        defaultHareketSicilShouldNotBeFound("isktoplam.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam is greater than or equal to DEFAULT_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.greaterThanOrEqual=" + DEFAULT_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam is greater than or equal to UPDATED_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.greaterThanOrEqual=" + UPDATED_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam is less than or equal to DEFAULT_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.lessThanOrEqual=" + DEFAULT_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam is less than or equal to SMALLER_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.lessThanOrEqual=" + SMALLER_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam is less than DEFAULT_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.lessThan=" + DEFAULT_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam is less than UPDATED_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.lessThan=" + UPDATED_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByIsktoplamIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where isktoplam is greater than DEFAULT_ISKTOPLAM
        defaultHareketSicilShouldNotBeFound("isktoplam.greaterThan=" + DEFAULT_ISKTOPLAM);

        // Get all the hareketSicilList where isktoplam is greater than SMALLER_ISKTOPLAM
        defaultHareketSicilShouldBeFound("isktoplam.greaterThan=" + SMALLER_ISKTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam equals to DEFAULT_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.equals=" + DEFAULT_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam equals to UPDATED_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.equals=" + UPDATED_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam in DEFAULT_KDVTOPLAM or UPDATED_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.in=" + DEFAULT_KDVTOPLAM + "," + UPDATED_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam equals to UPDATED_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.in=" + UPDATED_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam is not null
        defaultHareketSicilShouldBeFound("kdvtoplam.specified=true");

        // Get all the hareketSicilList where kdvtoplam is null
        defaultHareketSicilShouldNotBeFound("kdvtoplam.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam is greater than or equal to DEFAULT_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.greaterThanOrEqual=" + DEFAULT_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam is greater than or equal to UPDATED_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.greaterThanOrEqual=" + UPDATED_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam is less than or equal to DEFAULT_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.lessThanOrEqual=" + DEFAULT_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam is less than or equal to SMALLER_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.lessThanOrEqual=" + SMALLER_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam is less than DEFAULT_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.lessThan=" + DEFAULT_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam is less than UPDATED_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.lessThan=" + UPDATED_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByKdvtoplamIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where kdvtoplam is greater than DEFAULT_KDVTOPLAM
        defaultHareketSicilShouldNotBeFound("kdvtoplam.greaterThan=" + DEFAULT_KDVTOPLAM);

        // Get all the hareketSicilList where kdvtoplam is greater than SMALLER_KDVTOPLAM
        defaultHareketSicilShouldBeFound("kdvtoplam.greaterThan=" + SMALLER_KDVTOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam equals to DEFAULT_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.equals=" + DEFAULT_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam equals to UPDATED_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.equals=" + UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsInShouldWork() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam in DEFAULT_ARATOPLAM or UPDATED_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.in=" + DEFAULT_ARATOPLAM + "," + UPDATED_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam equals to UPDATED_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.in=" + UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam is not null
        defaultHareketSicilShouldBeFound("aratoplam.specified=true");

        // Get all the hareketSicilList where aratoplam is null
        defaultHareketSicilShouldNotBeFound("aratoplam.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam is greater than or equal to DEFAULT_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.greaterThanOrEqual=" + DEFAULT_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam is greater than or equal to UPDATED_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.greaterThanOrEqual=" + UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam is less than or equal to DEFAULT_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.lessThanOrEqual=" + DEFAULT_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam is less than or equal to SMALLER_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.lessThanOrEqual=" + SMALLER_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam is less than DEFAULT_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.lessThan=" + DEFAULT_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam is less than UPDATED_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.lessThan=" + UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByAratoplamIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        // Get all the hareketSicilList where aratoplam is greater than DEFAULT_ARATOPLAM
        defaultHareketSicilShouldNotBeFound("aratoplam.greaterThan=" + DEFAULT_ARATOPLAM);

        // Get all the hareketSicilList where aratoplam is greater than SMALLER_ARATOPLAM
        defaultHareketSicilShouldBeFound("aratoplam.greaterThan=" + SMALLER_ARATOPLAM);
    }

    @Test
    @Transactional
    void getAllHareketSicilsByCariIsEqualToSomething() throws Exception {
        Cari cari;
        if (TestUtil.findAll(em, Cari.class).isEmpty()) {
            hareketSicilRepository.saveAndFlush(hareketSicil);
            cari = CariResourceIT.createEntity(em);
        } else {
            cari = TestUtil.findAll(em, Cari.class).get(0);
        }
        em.persist(cari);
        em.flush();
        hareketSicil.setCari(cari);
        hareketSicilRepository.saveAndFlush(hareketSicil);
        Long cariId = cari.getId();

        // Get all the hareketSicilList where cari equals to cariId
        defaultHareketSicilShouldBeFound("cariId.equals=" + cariId);

        // Get all the hareketSicilList where cari equals to (cariId + 1)
        defaultHareketSicilShouldNotBeFound("cariId.equals=" + (cariId + 1));
    }

    @Test
    @Transactional
    void getAllHareketSicilsByHareketDetaysIsEqualToSomething() throws Exception {
        HareketDetay hareketDetays;
        if (TestUtil.findAll(em, HareketDetay.class).isEmpty()) {
            hareketSicilRepository.saveAndFlush(hareketSicil);
            hareketDetays = HareketDetayResourceIT.createEntity(em);
        } else {
            hareketDetays = TestUtil.findAll(em, HareketDetay.class).get(0);
        }
        em.persist(hareketDetays);
        em.flush();
        hareketSicil.addHareketDetays(hareketDetays);
        hareketSicilRepository.saveAndFlush(hareketSicil);
        Long hareketDetaysId = hareketDetays.getId();

        // Get all the hareketSicilList where hareketDetays equals to hareketDetaysId
        defaultHareketSicilShouldBeFound("hareketDetaysId.equals=" + hareketDetaysId);

        // Get all the hareketSicilList where hareketDetays equals to (hareketDetaysId + 1)
        defaultHareketSicilShouldNotBeFound("hareketDetaysId.equals=" + (hareketDetaysId + 1));
    }

    @Test
    @Transactional
    void getAllHareketSicilsBySicilIsEqualToSomething() throws Exception {
        Sicil sicil;
        if (TestUtil.findAll(em, Sicil.class).isEmpty()) {
            hareketSicilRepository.saveAndFlush(hareketSicil);
            sicil = SicilResourceIT.createEntity(em);
        } else {
            sicil = TestUtil.findAll(em, Sicil.class).get(0);
        }
        em.persist(sicil);
        em.flush();
        hareketSicil.setSicil(sicil);
        hareketSicilRepository.saveAndFlush(hareketSicil);
        Long sicilId = sicil.getId();

        // Get all the hareketSicilList where sicil equals to sicilId
        defaultHareketSicilShouldBeFound("sicilId.equals=" + sicilId);

        // Get all the hareketSicilList where sicil equals to (sicilId + 1)
        defaultHareketSicilShouldNotBeFound("sicilId.equals=" + (sicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultHareketSicilShouldBeFound(String filter) throws Exception {
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hareketSicil.getId().intValue())))
            .andExpect(jsonPath("$.[*].evraktipi").value(hasItem(DEFAULT_EVRAKTIPI.toString())))
            .andExpect(jsonPath("$.[*].evrakno").value(hasItem(DEFAULT_EVRAKNO)))
            .andExpect(jsonPath("$.[*].tarihi").value(hasItem(DEFAULT_TARIHI.toString())))
            .andExpect(jsonPath("$.[*].sevktarihi").value(hasItem(DEFAULT_SEVKTARIHI.toString())))
            .andExpect(jsonPath("$.[*].saati").value(hasItem(DEFAULT_SAATI)))
            .andExpect(jsonPath("$.[*].hesapno").value(hasItem(DEFAULT_HESAPNO)))
            .andExpect(jsonPath("$.[*].hesapadi").value(hasItem(DEFAULT_HESAPADI)))
            .andExpect(jsonPath("$.[*].adresi").value(hasItem(DEFAULT_ADRESI)))
            .andExpect(jsonPath("$.[*].ilce").value(hasItem(DEFAULT_ILCE)))
            .andExpect(jsonPath("$.[*].sehir").value(hasItem(DEFAULT_SEHIR)))
            .andExpect(jsonPath("$.[*].vda").value(hasItem(DEFAULT_VDA)))
            .andExpect(jsonPath("$.[*].tcno").value(hasItem(DEFAULT_TCNO)))
            .andExpect(jsonPath("$.[*].vno").value(hasItem(DEFAULT_VNO)))
            .andExpect(jsonPath("$.[*].telefon").value(hasItem(DEFAULT_TELEFON)))
            .andExpect(jsonPath("$.[*].gun").value(hasItem(DEFAULT_GUN)))
            .andExpect(jsonPath("$.[*].tutari").value(hasItem(sameNumber(DEFAULT_TUTARI))))
            .andExpect(jsonPath("$.[*].notlar").value(hasItem(DEFAULT_NOTLAR)))
            .andExpect(jsonPath("$.[*].depotipi").value(hasItem(DEFAULT_DEPOTIPI.toString())))
            .andExpect(jsonPath("$.[*].toplam").value(hasItem(sameNumber(DEFAULT_TOPLAM))))
            .andExpect(jsonPath("$.[*].isktoplam").value(hasItem(sameNumber(DEFAULT_ISKTOPLAM))))
            .andExpect(jsonPath("$.[*].kdvtoplam").value(hasItem(sameNumber(DEFAULT_KDVTOPLAM))))
            .andExpect(jsonPath("$.[*].aratoplam").value(hasItem(sameNumber(DEFAULT_ARATOPLAM))));

        // Check, that the count call also returns 1
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultHareketSicilShouldNotBeFound(String filter) throws Exception {
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restHareketSicilMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingHareketSicil() throws Exception {
        // Get the hareketSicil
        restHareketSicilMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingHareketSicil() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();

        // Update the hareketSicil
        HareketSicil updatedHareketSicil = hareketSicilRepository.findById(hareketSicil.getId()).get();
        // Disconnect from session so that the updates on updatedHareketSicil are not directly saved in db
        em.detach(updatedHareketSicil);
        updatedHareketSicil
            .evraktipi(UPDATED_EVRAKTIPI)
            .evrakno(UPDATED_EVRAKNO)
            .tarihi(UPDATED_TARIHI)
            .sevktarihi(UPDATED_SEVKTARIHI)
            .saati(UPDATED_SAATI)
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .vda(UPDATED_VDA)
            .tcno(UPDATED_TCNO)
            .vno(UPDATED_VNO)
            .telefon(UPDATED_TELEFON)
            .gun(UPDATED_GUN)
            .tutari(UPDATED_TUTARI)
            .notlar(UPDATED_NOTLAR)
            .depotipi(UPDATED_DEPOTIPI)
            .toplam(UPDATED_TOPLAM)
            .isktoplam(UPDATED_ISKTOPLAM)
            .kdvtoplam(UPDATED_KDVTOPLAM)
            .aratoplam(UPDATED_ARATOPLAM);
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(updatedHareketSicil);

        restHareketSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hareketSicilDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isOk());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
        HareketSicil testHareketSicil = hareketSicilList.get(hareketSicilList.size() - 1);
        assertThat(testHareketSicil.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testHareketSicil.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testHareketSicil.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testHareketSicil.getSevktarihi()).isEqualTo(UPDATED_SEVKTARIHI);
        assertThat(testHareketSicil.getSaati()).isEqualTo(UPDATED_SAATI);
        assertThat(testHareketSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testHareketSicil.getHesapadi()).isEqualTo(UPDATED_HESAPADI);
        assertThat(testHareketSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testHareketSicil.getIlce()).isEqualTo(UPDATED_ILCE);
        assertThat(testHareketSicil.getSehir()).isEqualTo(UPDATED_SEHIR);
        assertThat(testHareketSicil.getVda()).isEqualTo(UPDATED_VDA);
        assertThat(testHareketSicil.getTcno()).isEqualTo(UPDATED_TCNO);
        assertThat(testHareketSicil.getVno()).isEqualTo(UPDATED_VNO);
        assertThat(testHareketSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testHareketSicil.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testHareketSicil.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
        assertThat(testHareketSicil.getNotlar()).isEqualTo(UPDATED_NOTLAR);
        assertThat(testHareketSicil.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testHareketSicil.getToplam()).isEqualByComparingTo(UPDATED_TOPLAM);
        assertThat(testHareketSicil.getIsktoplam()).isEqualByComparingTo(UPDATED_ISKTOPLAM);
        assertThat(testHareketSicil.getKdvtoplam()).isEqualByComparingTo(UPDATED_KDVTOPLAM);
        assertThat(testHareketSicil.getAratoplam()).isEqualByComparingTo(UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void putNonExistingHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hareketSicilDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateHareketSicilWithPatch() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();

        // Update the hareketSicil using partial update
        HareketSicil partialUpdatedHareketSicil = new HareketSicil();
        partialUpdatedHareketSicil.setId(hareketSicil.getId());

        partialUpdatedHareketSicil
            .evrakno(UPDATED_EVRAKNO)
            .sevktarihi(UPDATED_SEVKTARIHI)
            .hesapno(UPDATED_HESAPNO)
            .adresi(UPDATED_ADRESI)
            .vda(UPDATED_VDA)
            .tcno(UPDATED_TCNO)
            .vno(UPDATED_VNO)
            .telefon(UPDATED_TELEFON)
            .tutari(UPDATED_TUTARI)
            .toplam(UPDATED_TOPLAM)
            .isktoplam(UPDATED_ISKTOPLAM)
            .kdvtoplam(UPDATED_KDVTOPLAM);

        restHareketSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHareketSicil.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHareketSicil))
            )
            .andExpect(status().isOk());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
        HareketSicil testHareketSicil = hareketSicilList.get(hareketSicilList.size() - 1);
        assertThat(testHareketSicil.getEvraktipi()).isEqualTo(DEFAULT_EVRAKTIPI);
        assertThat(testHareketSicil.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testHareketSicil.getTarihi()).isEqualTo(DEFAULT_TARIHI);
        assertThat(testHareketSicil.getSevktarihi()).isEqualTo(UPDATED_SEVKTARIHI);
        assertThat(testHareketSicil.getSaati()).isEqualTo(DEFAULT_SAATI);
        assertThat(testHareketSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testHareketSicil.getHesapadi()).isEqualTo(DEFAULT_HESAPADI);
        assertThat(testHareketSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testHareketSicil.getIlce()).isEqualTo(DEFAULT_ILCE);
        assertThat(testHareketSicil.getSehir()).isEqualTo(DEFAULT_SEHIR);
        assertThat(testHareketSicil.getVda()).isEqualTo(UPDATED_VDA);
        assertThat(testHareketSicil.getTcno()).isEqualTo(UPDATED_TCNO);
        assertThat(testHareketSicil.getVno()).isEqualTo(UPDATED_VNO);
        assertThat(testHareketSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testHareketSicil.getGun()).isEqualTo(DEFAULT_GUN);
        assertThat(testHareketSicil.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
        assertThat(testHareketSicil.getNotlar()).isEqualTo(DEFAULT_NOTLAR);
        assertThat(testHareketSicil.getDepotipi()).isEqualTo(DEFAULT_DEPOTIPI);
        assertThat(testHareketSicil.getToplam()).isEqualByComparingTo(UPDATED_TOPLAM);
        assertThat(testHareketSicil.getIsktoplam()).isEqualByComparingTo(UPDATED_ISKTOPLAM);
        assertThat(testHareketSicil.getKdvtoplam()).isEqualByComparingTo(UPDATED_KDVTOPLAM);
        assertThat(testHareketSicil.getAratoplam()).isEqualByComparingTo(DEFAULT_ARATOPLAM);
    }

    @Test
    @Transactional
    void fullUpdateHareketSicilWithPatch() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();

        // Update the hareketSicil using partial update
        HareketSicil partialUpdatedHareketSicil = new HareketSicil();
        partialUpdatedHareketSicil.setId(hareketSicil.getId());

        partialUpdatedHareketSicil
            .evraktipi(UPDATED_EVRAKTIPI)
            .evrakno(UPDATED_EVRAKNO)
            .tarihi(UPDATED_TARIHI)
            .sevktarihi(UPDATED_SEVKTARIHI)
            .saati(UPDATED_SAATI)
            .hesapno(UPDATED_HESAPNO)
            .hesapadi(UPDATED_HESAPADI)
            .adresi(UPDATED_ADRESI)
            .ilce(UPDATED_ILCE)
            .sehir(UPDATED_SEHIR)
            .vda(UPDATED_VDA)
            .tcno(UPDATED_TCNO)
            .vno(UPDATED_VNO)
            .telefon(UPDATED_TELEFON)
            .gun(UPDATED_GUN)
            .tutari(UPDATED_TUTARI)
            .notlar(UPDATED_NOTLAR)
            .depotipi(UPDATED_DEPOTIPI)
            .toplam(UPDATED_TOPLAM)
            .isktoplam(UPDATED_ISKTOPLAM)
            .kdvtoplam(UPDATED_KDVTOPLAM)
            .aratoplam(UPDATED_ARATOPLAM);

        restHareketSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHareketSicil.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHareketSicil))
            )
            .andExpect(status().isOk());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
        HareketSicil testHareketSicil = hareketSicilList.get(hareketSicilList.size() - 1);
        assertThat(testHareketSicil.getEvraktipi()).isEqualTo(UPDATED_EVRAKTIPI);
        assertThat(testHareketSicil.getEvrakno()).isEqualTo(UPDATED_EVRAKNO);
        assertThat(testHareketSicil.getTarihi()).isEqualTo(UPDATED_TARIHI);
        assertThat(testHareketSicil.getSevktarihi()).isEqualTo(UPDATED_SEVKTARIHI);
        assertThat(testHareketSicil.getSaati()).isEqualTo(UPDATED_SAATI);
        assertThat(testHareketSicil.getHesapno()).isEqualTo(UPDATED_HESAPNO);
        assertThat(testHareketSicil.getHesapadi()).isEqualTo(UPDATED_HESAPADI);
        assertThat(testHareketSicil.getAdresi()).isEqualTo(UPDATED_ADRESI);
        assertThat(testHareketSicil.getIlce()).isEqualTo(UPDATED_ILCE);
        assertThat(testHareketSicil.getSehir()).isEqualTo(UPDATED_SEHIR);
        assertThat(testHareketSicil.getVda()).isEqualTo(UPDATED_VDA);
        assertThat(testHareketSicil.getTcno()).isEqualTo(UPDATED_TCNO);
        assertThat(testHareketSicil.getVno()).isEqualTo(UPDATED_VNO);
        assertThat(testHareketSicil.getTelefon()).isEqualTo(UPDATED_TELEFON);
        assertThat(testHareketSicil.getGun()).isEqualTo(UPDATED_GUN);
        assertThat(testHareketSicil.getTutari()).isEqualByComparingTo(UPDATED_TUTARI);
        assertThat(testHareketSicil.getNotlar()).isEqualTo(UPDATED_NOTLAR);
        assertThat(testHareketSicil.getDepotipi()).isEqualTo(UPDATED_DEPOTIPI);
        assertThat(testHareketSicil.getToplam()).isEqualByComparingTo(UPDATED_TOPLAM);
        assertThat(testHareketSicil.getIsktoplam()).isEqualByComparingTo(UPDATED_ISKTOPLAM);
        assertThat(testHareketSicil.getKdvtoplam()).isEqualByComparingTo(UPDATED_KDVTOPLAM);
        assertThat(testHareketSicil.getAratoplam()).isEqualByComparingTo(UPDATED_ARATOPLAM);
    }

    @Test
    @Transactional
    void patchNonExistingHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, hareketSicilDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamHareketSicil() throws Exception {
        int databaseSizeBeforeUpdate = hareketSicilRepository.findAll().size();
        hareketSicil.setId(count.incrementAndGet());

        // Create the HareketSicil
        HareketSicilDTO hareketSicilDTO = hareketSicilMapper.toDto(hareketSicil);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketSicilMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketSicilDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HareketSicil in the database
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHareketSicil() throws Exception {
        // Initialize the database
        hareketSicilRepository.saveAndFlush(hareketSicil);

        int databaseSizeBeforeDelete = hareketSicilRepository.findAll().size();

        // Delete the hareketSicil
        restHareketSicilMockMvc
            .perform(delete(ENTITY_API_URL_ID, hareketSicil.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<HareketSicil> hareketSicilList = hareketSicilRepository.findAll();
        assertThat(hareketSicilList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
