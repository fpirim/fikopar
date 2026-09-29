package com.dizel.fikopar.web.rest;

import static com.dizel.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.HareketDetay;
import com.dizel.fikopar.domain.HareketSicil;
import com.dizel.fikopar.repository.HareketDetayRepository;
import com.dizel.fikopar.service.criteria.HareketDetayCriteria;
import com.dizel.fikopar.service.dto.HareketDetayDTO;
import com.dizel.fikopar.service.mapper.HareketDetayMapper;
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
 * Integration tests for the {@link HareketDetayResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class HareketDetayResourceIT {

    private static final String DEFAULT_STOKNO = "AAAAAAAAAA";
    private static final String UPDATED_STOKNO = "BBBBBBBBBB";

    private static final String DEFAULT_PARCANO = "AAAAAAAAAA";
    private static final String UPDATED_PARCANO = "BBBBBBBBBB";

    private static final String DEFAULT_TIPI = "AAAAAAAAAA";
    private static final String UPDATED_TIPI = "BBBBBBBBBB";

    private static final String DEFAULT_CINSI = "AAAAAAAAAA";
    private static final String UPDATED_CINSI = "BBBBBBBBBB";

    private static final String DEFAULT_MARKA = "AAAAAAAAAA";
    private static final String UPDATED_MARKA = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_ISKONTO = new BigDecimal(1);
    private static final BigDecimal UPDATED_ISKONTO = new BigDecimal(2);
    private static final BigDecimal SMALLER_ISKONTO = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_KDV = new BigDecimal(1);
    private static final BigDecimal UPDATED_KDV = new BigDecimal(2);
    private static final BigDecimal SMALLER_KDV = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_ADET = new BigDecimal(1);
    private static final BigDecimal UPDATED_ADET = new BigDecimal(2);
    private static final BigDecimal SMALLER_ADET = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_FIYATI = new BigDecimal(1);
    private static final BigDecimal UPDATED_FIYATI = new BigDecimal(2);
    private static final BigDecimal SMALLER_FIYATI = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/hareket-detays";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private HareketDetayRepository hareketDetayRepository;

    @Autowired
    private HareketDetayMapper hareketDetayMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restHareketDetayMockMvc;

    private HareketDetay hareketDetay;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HareketDetay createEntity(EntityManager em) {
        HareketDetay hareketDetay = new HareketDetay()
            .stokno(DEFAULT_STOKNO)
            .parcano(DEFAULT_PARCANO)
            .tipi(DEFAULT_TIPI)
            .cinsi(DEFAULT_CINSI)
            .marka(DEFAULT_MARKA)
            .iskonto(DEFAULT_ISKONTO)
            .kdv(DEFAULT_KDV)
            .adet(DEFAULT_ADET)
            .fiyati(DEFAULT_FIYATI);
        return hareketDetay;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static HareketDetay createUpdatedEntity(EntityManager em) {
        HareketDetay hareketDetay = new HareketDetay()
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .iskonto(UPDATED_ISKONTO)
            .kdv(UPDATED_KDV)
            .adet(UPDATED_ADET)
            .fiyati(UPDATED_FIYATI);
        return hareketDetay;
    }

    @BeforeEach
    public void initTest() {
        hareketDetay = createEntity(em);
    }

    @Test
    @Transactional
    void createHareketDetay() throws Exception {
        int databaseSizeBeforeCreate = hareketDetayRepository.findAll().size();
        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);
        restHareketDetayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isCreated());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeCreate + 1);
        HareketDetay testHareketDetay = hareketDetayList.get(hareketDetayList.size() - 1);
        assertThat(testHareketDetay.getStokno()).isEqualTo(DEFAULT_STOKNO);
        assertThat(testHareketDetay.getParcano()).isEqualTo(DEFAULT_PARCANO);
        assertThat(testHareketDetay.getTipi()).isEqualTo(DEFAULT_TIPI);
        assertThat(testHareketDetay.getCinsi()).isEqualTo(DEFAULT_CINSI);
        assertThat(testHareketDetay.getMarka()).isEqualTo(DEFAULT_MARKA);
        assertThat(testHareketDetay.getIskonto()).isEqualByComparingTo(DEFAULT_ISKONTO);
        assertThat(testHareketDetay.getKdv()).isEqualByComparingTo(DEFAULT_KDV);
        assertThat(testHareketDetay.getAdet()).isEqualByComparingTo(DEFAULT_ADET);
        assertThat(testHareketDetay.getFiyati()).isEqualByComparingTo(DEFAULT_FIYATI);
    }

    @Test
    @Transactional
    void createHareketDetayWithExistingId() throws Exception {
        // Create the HareketDetay with an existing ID
        hareketDetay.setId(1L);
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        int databaseSizeBeforeCreate = hareketDetayRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restHareketDetayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllHareketDetays() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hareketDetay.getId().intValue())))
            .andExpect(jsonPath("$.[*].stokno").value(hasItem(DEFAULT_STOKNO)))
            .andExpect(jsonPath("$.[*].parcano").value(hasItem(DEFAULT_PARCANO)))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI)))
            .andExpect(jsonPath("$.[*].cinsi").value(hasItem(DEFAULT_CINSI)))
            .andExpect(jsonPath("$.[*].marka").value(hasItem(DEFAULT_MARKA)))
            .andExpect(jsonPath("$.[*].iskonto").value(hasItem(sameNumber(DEFAULT_ISKONTO))))
            .andExpect(jsonPath("$.[*].kdv").value(hasItem(sameNumber(DEFAULT_KDV))))
            .andExpect(jsonPath("$.[*].adet").value(hasItem(sameNumber(DEFAULT_ADET))))
            .andExpect(jsonPath("$.[*].fiyati").value(hasItem(sameNumber(DEFAULT_FIYATI))));
    }

    @Test
    @Transactional
    void getHareketDetay() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get the hareketDetay
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL_ID, hareketDetay.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(hareketDetay.getId().intValue()))
            .andExpect(jsonPath("$.stokno").value(DEFAULT_STOKNO))
            .andExpect(jsonPath("$.parcano").value(DEFAULT_PARCANO))
            .andExpect(jsonPath("$.tipi").value(DEFAULT_TIPI))
            .andExpect(jsonPath("$.cinsi").value(DEFAULT_CINSI))
            .andExpect(jsonPath("$.marka").value(DEFAULT_MARKA))
            .andExpect(jsonPath("$.iskonto").value(sameNumber(DEFAULT_ISKONTO)))
            .andExpect(jsonPath("$.kdv").value(sameNumber(DEFAULT_KDV)))
            .andExpect(jsonPath("$.adet").value(sameNumber(DEFAULT_ADET)))
            .andExpect(jsonPath("$.fiyati").value(sameNumber(DEFAULT_FIYATI)));
    }

    @Test
    @Transactional
    void getHareketDetaysByIdFiltering() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        Long id = hareketDetay.getId();

        defaultHareketDetayShouldBeFound("id.equals=" + id);
        defaultHareketDetayShouldNotBeFound("id.notEquals=" + id);

        defaultHareketDetayShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultHareketDetayShouldNotBeFound("id.greaterThan=" + id);

        defaultHareketDetayShouldBeFound("id.lessThanOrEqual=" + id);
        defaultHareketDetayShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByStoknoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where stokno equals to DEFAULT_STOKNO
        defaultHareketDetayShouldBeFound("stokno.equals=" + DEFAULT_STOKNO);

        // Get all the hareketDetayList where stokno equals to UPDATED_STOKNO
        defaultHareketDetayShouldNotBeFound("stokno.equals=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByStoknoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where stokno in DEFAULT_STOKNO or UPDATED_STOKNO
        defaultHareketDetayShouldBeFound("stokno.in=" + DEFAULT_STOKNO + "," + UPDATED_STOKNO);

        // Get all the hareketDetayList where stokno equals to UPDATED_STOKNO
        defaultHareketDetayShouldNotBeFound("stokno.in=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByStoknoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where stokno is not null
        defaultHareketDetayShouldBeFound("stokno.specified=true");

        // Get all the hareketDetayList where stokno is null
        defaultHareketDetayShouldNotBeFound("stokno.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByStoknoContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where stokno contains DEFAULT_STOKNO
        defaultHareketDetayShouldBeFound("stokno.contains=" + DEFAULT_STOKNO);

        // Get all the hareketDetayList where stokno contains UPDATED_STOKNO
        defaultHareketDetayShouldNotBeFound("stokno.contains=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByStoknoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where stokno does not contain DEFAULT_STOKNO
        defaultHareketDetayShouldNotBeFound("stokno.doesNotContain=" + DEFAULT_STOKNO);

        // Get all the hareketDetayList where stokno does not contain UPDATED_STOKNO
        defaultHareketDetayShouldBeFound("stokno.doesNotContain=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByParcanoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where parcano equals to DEFAULT_PARCANO
        defaultHareketDetayShouldBeFound("parcano.equals=" + DEFAULT_PARCANO);

        // Get all the hareketDetayList where parcano equals to UPDATED_PARCANO
        defaultHareketDetayShouldNotBeFound("parcano.equals=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByParcanoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where parcano in DEFAULT_PARCANO or UPDATED_PARCANO
        defaultHareketDetayShouldBeFound("parcano.in=" + DEFAULT_PARCANO + "," + UPDATED_PARCANO);

        // Get all the hareketDetayList where parcano equals to UPDATED_PARCANO
        defaultHareketDetayShouldNotBeFound("parcano.in=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByParcanoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where parcano is not null
        defaultHareketDetayShouldBeFound("parcano.specified=true");

        // Get all the hareketDetayList where parcano is null
        defaultHareketDetayShouldNotBeFound("parcano.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByParcanoContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where parcano contains DEFAULT_PARCANO
        defaultHareketDetayShouldBeFound("parcano.contains=" + DEFAULT_PARCANO);

        // Get all the hareketDetayList where parcano contains UPDATED_PARCANO
        defaultHareketDetayShouldNotBeFound("parcano.contains=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByParcanoNotContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where parcano does not contain DEFAULT_PARCANO
        defaultHareketDetayShouldNotBeFound("parcano.doesNotContain=" + DEFAULT_PARCANO);

        // Get all the hareketDetayList where parcano does not contain UPDATED_PARCANO
        defaultHareketDetayShouldBeFound("parcano.doesNotContain=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByTipiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where tipi equals to DEFAULT_TIPI
        defaultHareketDetayShouldBeFound("tipi.equals=" + DEFAULT_TIPI);

        // Get all the hareketDetayList where tipi equals to UPDATED_TIPI
        defaultHareketDetayShouldNotBeFound("tipi.equals=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByTipiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where tipi in DEFAULT_TIPI or UPDATED_TIPI
        defaultHareketDetayShouldBeFound("tipi.in=" + DEFAULT_TIPI + "," + UPDATED_TIPI);

        // Get all the hareketDetayList where tipi equals to UPDATED_TIPI
        defaultHareketDetayShouldNotBeFound("tipi.in=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByTipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where tipi is not null
        defaultHareketDetayShouldBeFound("tipi.specified=true");

        // Get all the hareketDetayList where tipi is null
        defaultHareketDetayShouldNotBeFound("tipi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByTipiContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where tipi contains DEFAULT_TIPI
        defaultHareketDetayShouldBeFound("tipi.contains=" + DEFAULT_TIPI);

        // Get all the hareketDetayList where tipi contains UPDATED_TIPI
        defaultHareketDetayShouldNotBeFound("tipi.contains=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByTipiNotContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where tipi does not contain DEFAULT_TIPI
        defaultHareketDetayShouldNotBeFound("tipi.doesNotContain=" + DEFAULT_TIPI);

        // Get all the hareketDetayList where tipi does not contain UPDATED_TIPI
        defaultHareketDetayShouldBeFound("tipi.doesNotContain=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByCinsiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where cinsi equals to DEFAULT_CINSI
        defaultHareketDetayShouldBeFound("cinsi.equals=" + DEFAULT_CINSI);

        // Get all the hareketDetayList where cinsi equals to UPDATED_CINSI
        defaultHareketDetayShouldNotBeFound("cinsi.equals=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByCinsiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where cinsi in DEFAULT_CINSI or UPDATED_CINSI
        defaultHareketDetayShouldBeFound("cinsi.in=" + DEFAULT_CINSI + "," + UPDATED_CINSI);

        // Get all the hareketDetayList where cinsi equals to UPDATED_CINSI
        defaultHareketDetayShouldNotBeFound("cinsi.in=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByCinsiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where cinsi is not null
        defaultHareketDetayShouldBeFound("cinsi.specified=true");

        // Get all the hareketDetayList where cinsi is null
        defaultHareketDetayShouldNotBeFound("cinsi.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByCinsiContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where cinsi contains DEFAULT_CINSI
        defaultHareketDetayShouldBeFound("cinsi.contains=" + DEFAULT_CINSI);

        // Get all the hareketDetayList where cinsi contains UPDATED_CINSI
        defaultHareketDetayShouldNotBeFound("cinsi.contains=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByCinsiNotContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where cinsi does not contain DEFAULT_CINSI
        defaultHareketDetayShouldNotBeFound("cinsi.doesNotContain=" + DEFAULT_CINSI);

        // Get all the hareketDetayList where cinsi does not contain UPDATED_CINSI
        defaultHareketDetayShouldBeFound("cinsi.doesNotContain=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByMarkaIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where marka equals to DEFAULT_MARKA
        defaultHareketDetayShouldBeFound("marka.equals=" + DEFAULT_MARKA);

        // Get all the hareketDetayList where marka equals to UPDATED_MARKA
        defaultHareketDetayShouldNotBeFound("marka.equals=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByMarkaIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where marka in DEFAULT_MARKA or UPDATED_MARKA
        defaultHareketDetayShouldBeFound("marka.in=" + DEFAULT_MARKA + "," + UPDATED_MARKA);

        // Get all the hareketDetayList where marka equals to UPDATED_MARKA
        defaultHareketDetayShouldNotBeFound("marka.in=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByMarkaIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where marka is not null
        defaultHareketDetayShouldBeFound("marka.specified=true");

        // Get all the hareketDetayList where marka is null
        defaultHareketDetayShouldNotBeFound("marka.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByMarkaContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where marka contains DEFAULT_MARKA
        defaultHareketDetayShouldBeFound("marka.contains=" + DEFAULT_MARKA);

        // Get all the hareketDetayList where marka contains UPDATED_MARKA
        defaultHareketDetayShouldNotBeFound("marka.contains=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByMarkaNotContainsSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where marka does not contain DEFAULT_MARKA
        defaultHareketDetayShouldNotBeFound("marka.doesNotContain=" + DEFAULT_MARKA);

        // Get all the hareketDetayList where marka does not contain UPDATED_MARKA
        defaultHareketDetayShouldBeFound("marka.doesNotContain=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto equals to DEFAULT_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.equals=" + DEFAULT_ISKONTO);

        // Get all the hareketDetayList where iskonto equals to UPDATED_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.equals=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto in DEFAULT_ISKONTO or UPDATED_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.in=" + DEFAULT_ISKONTO + "," + UPDATED_ISKONTO);

        // Get all the hareketDetayList where iskonto equals to UPDATED_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.in=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto is not null
        defaultHareketDetayShouldBeFound("iskonto.specified=true");

        // Get all the hareketDetayList where iskonto is null
        defaultHareketDetayShouldNotBeFound("iskonto.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto is greater than or equal to DEFAULT_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.greaterThanOrEqual=" + DEFAULT_ISKONTO);

        // Get all the hareketDetayList where iskonto is greater than or equal to UPDATED_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.greaterThanOrEqual=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto is less than or equal to DEFAULT_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.lessThanOrEqual=" + DEFAULT_ISKONTO);

        // Get all the hareketDetayList where iskonto is less than or equal to SMALLER_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.lessThanOrEqual=" + SMALLER_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto is less than DEFAULT_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.lessThan=" + DEFAULT_ISKONTO);

        // Get all the hareketDetayList where iskonto is less than UPDATED_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.lessThan=" + UPDATED_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByIskontoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where iskonto is greater than DEFAULT_ISKONTO
        defaultHareketDetayShouldNotBeFound("iskonto.greaterThan=" + DEFAULT_ISKONTO);

        // Get all the hareketDetayList where iskonto is greater than SMALLER_ISKONTO
        defaultHareketDetayShouldBeFound("iskonto.greaterThan=" + SMALLER_ISKONTO);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv equals to DEFAULT_KDV
        defaultHareketDetayShouldBeFound("kdv.equals=" + DEFAULT_KDV);

        // Get all the hareketDetayList where kdv equals to UPDATED_KDV
        defaultHareketDetayShouldNotBeFound("kdv.equals=" + UPDATED_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv in DEFAULT_KDV or UPDATED_KDV
        defaultHareketDetayShouldBeFound("kdv.in=" + DEFAULT_KDV + "," + UPDATED_KDV);

        // Get all the hareketDetayList where kdv equals to UPDATED_KDV
        defaultHareketDetayShouldNotBeFound("kdv.in=" + UPDATED_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv is not null
        defaultHareketDetayShouldBeFound("kdv.specified=true");

        // Get all the hareketDetayList where kdv is null
        defaultHareketDetayShouldNotBeFound("kdv.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv is greater than or equal to DEFAULT_KDV
        defaultHareketDetayShouldBeFound("kdv.greaterThanOrEqual=" + DEFAULT_KDV);

        // Get all the hareketDetayList where kdv is greater than or equal to UPDATED_KDV
        defaultHareketDetayShouldNotBeFound("kdv.greaterThanOrEqual=" + UPDATED_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv is less than or equal to DEFAULT_KDV
        defaultHareketDetayShouldBeFound("kdv.lessThanOrEqual=" + DEFAULT_KDV);

        // Get all the hareketDetayList where kdv is less than or equal to SMALLER_KDV
        defaultHareketDetayShouldNotBeFound("kdv.lessThanOrEqual=" + SMALLER_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv is less than DEFAULT_KDV
        defaultHareketDetayShouldNotBeFound("kdv.lessThan=" + DEFAULT_KDV);

        // Get all the hareketDetayList where kdv is less than UPDATED_KDV
        defaultHareketDetayShouldBeFound("kdv.lessThan=" + UPDATED_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByKdvIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where kdv is greater than DEFAULT_KDV
        defaultHareketDetayShouldNotBeFound("kdv.greaterThan=" + DEFAULT_KDV);

        // Get all the hareketDetayList where kdv is greater than SMALLER_KDV
        defaultHareketDetayShouldBeFound("kdv.greaterThan=" + SMALLER_KDV);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet equals to DEFAULT_ADET
        defaultHareketDetayShouldBeFound("adet.equals=" + DEFAULT_ADET);

        // Get all the hareketDetayList where adet equals to UPDATED_ADET
        defaultHareketDetayShouldNotBeFound("adet.equals=" + UPDATED_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet in DEFAULT_ADET or UPDATED_ADET
        defaultHareketDetayShouldBeFound("adet.in=" + DEFAULT_ADET + "," + UPDATED_ADET);

        // Get all the hareketDetayList where adet equals to UPDATED_ADET
        defaultHareketDetayShouldNotBeFound("adet.in=" + UPDATED_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet is not null
        defaultHareketDetayShouldBeFound("adet.specified=true");

        // Get all the hareketDetayList where adet is null
        defaultHareketDetayShouldNotBeFound("adet.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet is greater than or equal to DEFAULT_ADET
        defaultHareketDetayShouldBeFound("adet.greaterThanOrEqual=" + DEFAULT_ADET);

        // Get all the hareketDetayList where adet is greater than or equal to UPDATED_ADET
        defaultHareketDetayShouldNotBeFound("adet.greaterThanOrEqual=" + UPDATED_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet is less than or equal to DEFAULT_ADET
        defaultHareketDetayShouldBeFound("adet.lessThanOrEqual=" + DEFAULT_ADET);

        // Get all the hareketDetayList where adet is less than or equal to SMALLER_ADET
        defaultHareketDetayShouldNotBeFound("adet.lessThanOrEqual=" + SMALLER_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet is less than DEFAULT_ADET
        defaultHareketDetayShouldNotBeFound("adet.lessThan=" + DEFAULT_ADET);

        // Get all the hareketDetayList where adet is less than UPDATED_ADET
        defaultHareketDetayShouldBeFound("adet.lessThan=" + UPDATED_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByAdetIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where adet is greater than DEFAULT_ADET
        defaultHareketDetayShouldNotBeFound("adet.greaterThan=" + DEFAULT_ADET);

        // Get all the hareketDetayList where adet is greater than SMALLER_ADET
        defaultHareketDetayShouldBeFound("adet.greaterThan=" + SMALLER_ADET);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati equals to DEFAULT_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.equals=" + DEFAULT_FIYATI);

        // Get all the hareketDetayList where fiyati equals to UPDATED_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.equals=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsInShouldWork() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati in DEFAULT_FIYATI or UPDATED_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.in=" + DEFAULT_FIYATI + "," + UPDATED_FIYATI);

        // Get all the hareketDetayList where fiyati equals to UPDATED_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.in=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsNullOrNotNull() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati is not null
        defaultHareketDetayShouldBeFound("fiyati.specified=true");

        // Get all the hareketDetayList where fiyati is null
        defaultHareketDetayShouldNotBeFound("fiyati.specified=false");
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati is greater than or equal to DEFAULT_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.greaterThanOrEqual=" + DEFAULT_FIYATI);

        // Get all the hareketDetayList where fiyati is greater than or equal to UPDATED_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.greaterThanOrEqual=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati is less than or equal to DEFAULT_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.lessThanOrEqual=" + DEFAULT_FIYATI);

        // Get all the hareketDetayList where fiyati is less than or equal to SMALLER_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.lessThanOrEqual=" + SMALLER_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsLessThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati is less than DEFAULT_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.lessThan=" + DEFAULT_FIYATI);

        // Get all the hareketDetayList where fiyati is less than UPDATED_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.lessThan=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByFiyatiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        // Get all the hareketDetayList where fiyati is greater than DEFAULT_FIYATI
        defaultHareketDetayShouldNotBeFound("fiyati.greaterThan=" + DEFAULT_FIYATI);

        // Get all the hareketDetayList where fiyati is greater than SMALLER_FIYATI
        defaultHareketDetayShouldBeFound("fiyati.greaterThan=" + SMALLER_FIYATI);
    }

    @Test
    @Transactional
    void getAllHareketDetaysByHareketSicilIsEqualToSomething() throws Exception {
        HareketSicil hareketSicil;
        if (TestUtil.findAll(em, HareketSicil.class).isEmpty()) {
            hareketDetayRepository.saveAndFlush(hareketDetay);
            hareketSicil = HareketSicilResourceIT.createEntity(em);
        } else {
            hareketSicil = TestUtil.findAll(em, HareketSicil.class).get(0);
        }
        em.persist(hareketSicil);
        em.flush();
        hareketDetay.setHareketSicil(hareketSicil);
        hareketDetayRepository.saveAndFlush(hareketDetay);
        Long hareketSicilId = hareketSicil.getId();

        // Get all the hareketDetayList where hareketSicil equals to hareketSicilId
        defaultHareketDetayShouldBeFound("hareketSicilId.equals=" + hareketSicilId);

        // Get all the hareketDetayList where hareketSicil equals to (hareketSicilId + 1)
        defaultHareketDetayShouldNotBeFound("hareketSicilId.equals=" + (hareketSicilId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultHareketDetayShouldBeFound(String filter) throws Exception {
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(hareketDetay.getId().intValue())))
            .andExpect(jsonPath("$.[*].stokno").value(hasItem(DEFAULT_STOKNO)))
            .andExpect(jsonPath("$.[*].parcano").value(hasItem(DEFAULT_PARCANO)))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI)))
            .andExpect(jsonPath("$.[*].cinsi").value(hasItem(DEFAULT_CINSI)))
            .andExpect(jsonPath("$.[*].marka").value(hasItem(DEFAULT_MARKA)))
            .andExpect(jsonPath("$.[*].iskonto").value(hasItem(sameNumber(DEFAULT_ISKONTO))))
            .andExpect(jsonPath("$.[*].kdv").value(hasItem(sameNumber(DEFAULT_KDV))))
            .andExpect(jsonPath("$.[*].adet").value(hasItem(sameNumber(DEFAULT_ADET))))
            .andExpect(jsonPath("$.[*].fiyati").value(hasItem(sameNumber(DEFAULT_FIYATI))));

        // Check, that the count call also returns 1
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultHareketDetayShouldNotBeFound(String filter) throws Exception {
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restHareketDetayMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingHareketDetay() throws Exception {
        // Get the hareketDetay
        restHareketDetayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingHareketDetay() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();

        // Update the hareketDetay
        HareketDetay updatedHareketDetay = hareketDetayRepository.findById(hareketDetay.getId()).get();
        // Disconnect from session so that the updates on updatedHareketDetay are not directly saved in db
        em.detach(updatedHareketDetay);
        updatedHareketDetay
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .iskonto(UPDATED_ISKONTO)
            .kdv(UPDATED_KDV)
            .adet(UPDATED_ADET)
            .fiyati(UPDATED_FIYATI);
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(updatedHareketDetay);

        restHareketDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hareketDetayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isOk());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
        HareketDetay testHareketDetay = hareketDetayList.get(hareketDetayList.size() - 1);
        assertThat(testHareketDetay.getStokno()).isEqualTo(UPDATED_STOKNO);
        assertThat(testHareketDetay.getParcano()).isEqualTo(UPDATED_PARCANO);
        assertThat(testHareketDetay.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testHareketDetay.getCinsi()).isEqualTo(UPDATED_CINSI);
        assertThat(testHareketDetay.getMarka()).isEqualTo(UPDATED_MARKA);
        assertThat(testHareketDetay.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testHareketDetay.getKdv()).isEqualByComparingTo(UPDATED_KDV);
        assertThat(testHareketDetay.getAdet()).isEqualByComparingTo(UPDATED_ADET);
        assertThat(testHareketDetay.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void putNonExistingHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, hareketDetayDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateHareketDetayWithPatch() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();

        // Update the hareketDetay using partial update
        HareketDetay partialUpdatedHareketDetay = new HareketDetay();
        partialUpdatedHareketDetay.setId(hareketDetay.getId());

        partialUpdatedHareketDetay
            .stokno(UPDATED_STOKNO)
            .tipi(UPDATED_TIPI)
            .iskonto(UPDATED_ISKONTO)
            .kdv(UPDATED_KDV)
            .adet(UPDATED_ADET)
            .fiyati(UPDATED_FIYATI);

        restHareketDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHareketDetay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHareketDetay))
            )
            .andExpect(status().isOk());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
        HareketDetay testHareketDetay = hareketDetayList.get(hareketDetayList.size() - 1);
        assertThat(testHareketDetay.getStokno()).isEqualTo(UPDATED_STOKNO);
        assertThat(testHareketDetay.getParcano()).isEqualTo(DEFAULT_PARCANO);
        assertThat(testHareketDetay.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testHareketDetay.getCinsi()).isEqualTo(DEFAULT_CINSI);
        assertThat(testHareketDetay.getMarka()).isEqualTo(DEFAULT_MARKA);
        assertThat(testHareketDetay.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testHareketDetay.getKdv()).isEqualByComparingTo(UPDATED_KDV);
        assertThat(testHareketDetay.getAdet()).isEqualByComparingTo(UPDATED_ADET);
        assertThat(testHareketDetay.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void fullUpdateHareketDetayWithPatch() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();

        // Update the hareketDetay using partial update
        HareketDetay partialUpdatedHareketDetay = new HareketDetay();
        partialUpdatedHareketDetay.setId(hareketDetay.getId());

        partialUpdatedHareketDetay
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .iskonto(UPDATED_ISKONTO)
            .kdv(UPDATED_KDV)
            .adet(UPDATED_ADET)
            .fiyati(UPDATED_FIYATI);

        restHareketDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedHareketDetay.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedHareketDetay))
            )
            .andExpect(status().isOk());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
        HareketDetay testHareketDetay = hareketDetayList.get(hareketDetayList.size() - 1);
        assertThat(testHareketDetay.getStokno()).isEqualTo(UPDATED_STOKNO);
        assertThat(testHareketDetay.getParcano()).isEqualTo(UPDATED_PARCANO);
        assertThat(testHareketDetay.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testHareketDetay.getCinsi()).isEqualTo(UPDATED_CINSI);
        assertThat(testHareketDetay.getMarka()).isEqualTo(UPDATED_MARKA);
        assertThat(testHareketDetay.getIskonto()).isEqualByComparingTo(UPDATED_ISKONTO);
        assertThat(testHareketDetay.getKdv()).isEqualByComparingTo(UPDATED_KDV);
        assertThat(testHareketDetay.getAdet()).isEqualByComparingTo(UPDATED_ADET);
        assertThat(testHareketDetay.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void patchNonExistingHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, hareketDetayDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamHareketDetay() throws Exception {
        int databaseSizeBeforeUpdate = hareketDetayRepository.findAll().size();
        hareketDetay.setId(count.incrementAndGet());

        // Create the HareketDetay
        HareketDetayDTO hareketDetayDTO = hareketDetayMapper.toDto(hareketDetay);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restHareketDetayMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(hareketDetayDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the HareketDetay in the database
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHareketDetay() throws Exception {
        // Initialize the database
        hareketDetayRepository.saveAndFlush(hareketDetay);

        int databaseSizeBeforeDelete = hareketDetayRepository.findAll().size();

        // Delete the hareketDetay
        restHareketDetayMockMvc
            .perform(delete(ENTITY_API_URL_ID, hareketDetay.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<HareketDetay> hareketDetayList = hareketDetayRepository.findAll();
        assertThat(hareketDetayList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
