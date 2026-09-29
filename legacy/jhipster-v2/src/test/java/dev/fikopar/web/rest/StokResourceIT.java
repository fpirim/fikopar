package dev.fikopar.web.rest;

import static dev.fikopar.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.fikopar.IntegrationTest;
import dev.fikopar.domain.Katalog;
import dev.fikopar.domain.Stok;
import dev.fikopar.repository.StokRepository;
import dev.fikopar.service.criteria.StokCriteria;
import dev.fikopar.service.dto.StokDTO;
import dev.fikopar.service.mapper.StokMapper;
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
 * Integration tests for the {@link StokResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class StokResourceIT {

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

    private static final BigDecimal DEFAULT_FIYATI = new BigDecimal(1);
    private static final BigDecimal UPDATED_FIYATI = new BigDecimal(2);
    private static final BigDecimal SMALLER_FIYATI = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/stoks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private StokRepository stokRepository;

    @Autowired
    private StokMapper stokMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restStokMockMvc;

    private Stok stok;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Stok createEntity(EntityManager em) {
        Stok stok = new Stok()
            .stokno(DEFAULT_STOKNO)
            .parcano(DEFAULT_PARCANO)
            .tipi(DEFAULT_TIPI)
            .cinsi(DEFAULT_CINSI)
            .marka(DEFAULT_MARKA)
            .fiyati(DEFAULT_FIYATI);
        return stok;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Stok createUpdatedEntity(EntityManager em) {
        Stok stok = new Stok()
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .fiyati(UPDATED_FIYATI);
        return stok;
    }

    @BeforeEach
    public void initTest() {
        stok = createEntity(em);
    }

    @Test
    @Transactional
    void createStok() throws Exception {
        int databaseSizeBeforeCreate = stokRepository.findAll().size();
        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);
        restStokMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(stokDTO)))
            .andExpect(status().isCreated());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeCreate + 1);
        Stok testStok = stokList.get(stokList.size() - 1);
        assertThat(testStok.getStokno()).isEqualTo(DEFAULT_STOKNO);
        assertThat(testStok.getParcano()).isEqualTo(DEFAULT_PARCANO);
        assertThat(testStok.getTipi()).isEqualTo(DEFAULT_TIPI);
        assertThat(testStok.getCinsi()).isEqualTo(DEFAULT_CINSI);
        assertThat(testStok.getMarka()).isEqualTo(DEFAULT_MARKA);
        assertThat(testStok.getFiyati()).isEqualByComparingTo(DEFAULT_FIYATI);
    }

    @Test
    @Transactional
    void createStokWithExistingId() throws Exception {
        // Create the Stok with an existing ID
        stok.setId(1L);
        StokDTO stokDTO = stokMapper.toDto(stok);

        int databaseSizeBeforeCreate = stokRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restStokMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(stokDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllStoks() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList
        restStokMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stok.getId().intValue())))
            .andExpect(jsonPath("$.[*].stokno").value(hasItem(DEFAULT_STOKNO)))
            .andExpect(jsonPath("$.[*].parcano").value(hasItem(DEFAULT_PARCANO)))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI)))
            .andExpect(jsonPath("$.[*].cinsi").value(hasItem(DEFAULT_CINSI)))
            .andExpect(jsonPath("$.[*].marka").value(hasItem(DEFAULT_MARKA)))
            .andExpect(jsonPath("$.[*].fiyati").value(hasItem(sameNumber(DEFAULT_FIYATI))));
    }

    @Test
    @Transactional
    void getStok() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get the stok
        restStokMockMvc
            .perform(get(ENTITY_API_URL_ID, stok.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(stok.getId().intValue()))
            .andExpect(jsonPath("$.stokno").value(DEFAULT_STOKNO))
            .andExpect(jsonPath("$.parcano").value(DEFAULT_PARCANO))
            .andExpect(jsonPath("$.tipi").value(DEFAULT_TIPI))
            .andExpect(jsonPath("$.cinsi").value(DEFAULT_CINSI))
            .andExpect(jsonPath("$.marka").value(DEFAULT_MARKA))
            .andExpect(jsonPath("$.fiyati").value(sameNumber(DEFAULT_FIYATI)));
    }

    @Test
    @Transactional
    void getStoksByIdFiltering() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        Long id = stok.getId();

        defaultStokShouldBeFound("id.equals=" + id);
        defaultStokShouldNotBeFound("id.notEquals=" + id);

        defaultStokShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultStokShouldNotBeFound("id.greaterThan=" + id);

        defaultStokShouldBeFound("id.lessThanOrEqual=" + id);
        defaultStokShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllStoksByStoknoIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where stokno equals to DEFAULT_STOKNO
        defaultStokShouldBeFound("stokno.equals=" + DEFAULT_STOKNO);

        // Get all the stokList where stokno equals to UPDATED_STOKNO
        defaultStokShouldNotBeFound("stokno.equals=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllStoksByStoknoIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where stokno in DEFAULT_STOKNO or UPDATED_STOKNO
        defaultStokShouldBeFound("stokno.in=" + DEFAULT_STOKNO + "," + UPDATED_STOKNO);

        // Get all the stokList where stokno equals to UPDATED_STOKNO
        defaultStokShouldNotBeFound("stokno.in=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllStoksByStoknoIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where stokno is not null
        defaultStokShouldBeFound("stokno.specified=true");

        // Get all the stokList where stokno is null
        defaultStokShouldNotBeFound("stokno.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByStoknoContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where stokno contains DEFAULT_STOKNO
        defaultStokShouldBeFound("stokno.contains=" + DEFAULT_STOKNO);

        // Get all the stokList where stokno contains UPDATED_STOKNO
        defaultStokShouldNotBeFound("stokno.contains=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllStoksByStoknoNotContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where stokno does not contain DEFAULT_STOKNO
        defaultStokShouldNotBeFound("stokno.doesNotContain=" + DEFAULT_STOKNO);

        // Get all the stokList where stokno does not contain UPDATED_STOKNO
        defaultStokShouldBeFound("stokno.doesNotContain=" + UPDATED_STOKNO);
    }

    @Test
    @Transactional
    void getAllStoksByParcanoIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where parcano equals to DEFAULT_PARCANO
        defaultStokShouldBeFound("parcano.equals=" + DEFAULT_PARCANO);

        // Get all the stokList where parcano equals to UPDATED_PARCANO
        defaultStokShouldNotBeFound("parcano.equals=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllStoksByParcanoIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where parcano in DEFAULT_PARCANO or UPDATED_PARCANO
        defaultStokShouldBeFound("parcano.in=" + DEFAULT_PARCANO + "," + UPDATED_PARCANO);

        // Get all the stokList where parcano equals to UPDATED_PARCANO
        defaultStokShouldNotBeFound("parcano.in=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllStoksByParcanoIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where parcano is not null
        defaultStokShouldBeFound("parcano.specified=true");

        // Get all the stokList where parcano is null
        defaultStokShouldNotBeFound("parcano.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByParcanoContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where parcano contains DEFAULT_PARCANO
        defaultStokShouldBeFound("parcano.contains=" + DEFAULT_PARCANO);

        // Get all the stokList where parcano contains UPDATED_PARCANO
        defaultStokShouldNotBeFound("parcano.contains=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllStoksByParcanoNotContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where parcano does not contain DEFAULT_PARCANO
        defaultStokShouldNotBeFound("parcano.doesNotContain=" + DEFAULT_PARCANO);

        // Get all the stokList where parcano does not contain UPDATED_PARCANO
        defaultStokShouldBeFound("parcano.doesNotContain=" + UPDATED_PARCANO);
    }

    @Test
    @Transactional
    void getAllStoksByTipiIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where tipi equals to DEFAULT_TIPI
        defaultStokShouldBeFound("tipi.equals=" + DEFAULT_TIPI);

        // Get all the stokList where tipi equals to UPDATED_TIPI
        defaultStokShouldNotBeFound("tipi.equals=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllStoksByTipiIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where tipi in DEFAULT_TIPI or UPDATED_TIPI
        defaultStokShouldBeFound("tipi.in=" + DEFAULT_TIPI + "," + UPDATED_TIPI);

        // Get all the stokList where tipi equals to UPDATED_TIPI
        defaultStokShouldNotBeFound("tipi.in=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllStoksByTipiIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where tipi is not null
        defaultStokShouldBeFound("tipi.specified=true");

        // Get all the stokList where tipi is null
        defaultStokShouldNotBeFound("tipi.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByTipiContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where tipi contains DEFAULT_TIPI
        defaultStokShouldBeFound("tipi.contains=" + DEFAULT_TIPI);

        // Get all the stokList where tipi contains UPDATED_TIPI
        defaultStokShouldNotBeFound("tipi.contains=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllStoksByTipiNotContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where tipi does not contain DEFAULT_TIPI
        defaultStokShouldNotBeFound("tipi.doesNotContain=" + DEFAULT_TIPI);

        // Get all the stokList where tipi does not contain UPDATED_TIPI
        defaultStokShouldBeFound("tipi.doesNotContain=" + UPDATED_TIPI);
    }

    @Test
    @Transactional
    void getAllStoksByCinsiIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where cinsi equals to DEFAULT_CINSI
        defaultStokShouldBeFound("cinsi.equals=" + DEFAULT_CINSI);

        // Get all the stokList where cinsi equals to UPDATED_CINSI
        defaultStokShouldNotBeFound("cinsi.equals=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllStoksByCinsiIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where cinsi in DEFAULT_CINSI or UPDATED_CINSI
        defaultStokShouldBeFound("cinsi.in=" + DEFAULT_CINSI + "," + UPDATED_CINSI);

        // Get all the stokList where cinsi equals to UPDATED_CINSI
        defaultStokShouldNotBeFound("cinsi.in=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllStoksByCinsiIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where cinsi is not null
        defaultStokShouldBeFound("cinsi.specified=true");

        // Get all the stokList where cinsi is null
        defaultStokShouldNotBeFound("cinsi.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByCinsiContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where cinsi contains DEFAULT_CINSI
        defaultStokShouldBeFound("cinsi.contains=" + DEFAULT_CINSI);

        // Get all the stokList where cinsi contains UPDATED_CINSI
        defaultStokShouldNotBeFound("cinsi.contains=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllStoksByCinsiNotContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where cinsi does not contain DEFAULT_CINSI
        defaultStokShouldNotBeFound("cinsi.doesNotContain=" + DEFAULT_CINSI);

        // Get all the stokList where cinsi does not contain UPDATED_CINSI
        defaultStokShouldBeFound("cinsi.doesNotContain=" + UPDATED_CINSI);
    }

    @Test
    @Transactional
    void getAllStoksByMarkaIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where marka equals to DEFAULT_MARKA
        defaultStokShouldBeFound("marka.equals=" + DEFAULT_MARKA);

        // Get all the stokList where marka equals to UPDATED_MARKA
        defaultStokShouldNotBeFound("marka.equals=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllStoksByMarkaIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where marka in DEFAULT_MARKA or UPDATED_MARKA
        defaultStokShouldBeFound("marka.in=" + DEFAULT_MARKA + "," + UPDATED_MARKA);

        // Get all the stokList where marka equals to UPDATED_MARKA
        defaultStokShouldNotBeFound("marka.in=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllStoksByMarkaIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where marka is not null
        defaultStokShouldBeFound("marka.specified=true");

        // Get all the stokList where marka is null
        defaultStokShouldNotBeFound("marka.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByMarkaContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where marka contains DEFAULT_MARKA
        defaultStokShouldBeFound("marka.contains=" + DEFAULT_MARKA);

        // Get all the stokList where marka contains UPDATED_MARKA
        defaultStokShouldNotBeFound("marka.contains=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllStoksByMarkaNotContainsSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where marka does not contain DEFAULT_MARKA
        defaultStokShouldNotBeFound("marka.doesNotContain=" + DEFAULT_MARKA);

        // Get all the stokList where marka does not contain UPDATED_MARKA
        defaultStokShouldBeFound("marka.doesNotContain=" + UPDATED_MARKA);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati equals to DEFAULT_FIYATI
        defaultStokShouldBeFound("fiyati.equals=" + DEFAULT_FIYATI);

        // Get all the stokList where fiyati equals to UPDATED_FIYATI
        defaultStokShouldNotBeFound("fiyati.equals=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsInShouldWork() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati in DEFAULT_FIYATI or UPDATED_FIYATI
        defaultStokShouldBeFound("fiyati.in=" + DEFAULT_FIYATI + "," + UPDATED_FIYATI);

        // Get all the stokList where fiyati equals to UPDATED_FIYATI
        defaultStokShouldNotBeFound("fiyati.in=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsNullOrNotNull() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati is not null
        defaultStokShouldBeFound("fiyati.specified=true");

        // Get all the stokList where fiyati is null
        defaultStokShouldNotBeFound("fiyati.specified=false");
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati is greater than or equal to DEFAULT_FIYATI
        defaultStokShouldBeFound("fiyati.greaterThanOrEqual=" + DEFAULT_FIYATI);

        // Get all the stokList where fiyati is greater than or equal to UPDATED_FIYATI
        defaultStokShouldNotBeFound("fiyati.greaterThanOrEqual=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati is less than or equal to DEFAULT_FIYATI
        defaultStokShouldBeFound("fiyati.lessThanOrEqual=" + DEFAULT_FIYATI);

        // Get all the stokList where fiyati is less than or equal to SMALLER_FIYATI
        defaultStokShouldNotBeFound("fiyati.lessThanOrEqual=" + SMALLER_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsLessThanSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati is less than DEFAULT_FIYATI
        defaultStokShouldNotBeFound("fiyati.lessThan=" + DEFAULT_FIYATI);

        // Get all the stokList where fiyati is less than UPDATED_FIYATI
        defaultStokShouldBeFound("fiyati.lessThan=" + UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByFiyatiIsGreaterThanSomething() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        // Get all the stokList where fiyati is greater than DEFAULT_FIYATI
        defaultStokShouldNotBeFound("fiyati.greaterThan=" + DEFAULT_FIYATI);

        // Get all the stokList where fiyati is greater than SMALLER_FIYATI
        defaultStokShouldBeFound("fiyati.greaterThan=" + SMALLER_FIYATI);
    }

    @Test
    @Transactional
    void getAllStoksByKatalogIsEqualToSomething() throws Exception {
        Katalog katalog;
        if (TestUtil.findAll(em, Katalog.class).isEmpty()) {
            stokRepository.saveAndFlush(stok);
            katalog = KatalogResourceIT.createEntity(em);
        } else {
            katalog = TestUtil.findAll(em, Katalog.class).get(0);
        }
        em.persist(katalog);
        em.flush();
        stok.setKatalog(katalog);
        stokRepository.saveAndFlush(stok);
        Long katalogId = katalog.getId();

        // Get all the stokList where katalog equals to katalogId
        defaultStokShouldBeFound("katalogId.equals=" + katalogId);

        // Get all the stokList where katalog equals to (katalogId + 1)
        defaultStokShouldNotBeFound("katalogId.equals=" + (katalogId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultStokShouldBeFound(String filter) throws Exception {
        restStokMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(stok.getId().intValue())))
            .andExpect(jsonPath("$.[*].stokno").value(hasItem(DEFAULT_STOKNO)))
            .andExpect(jsonPath("$.[*].parcano").value(hasItem(DEFAULT_PARCANO)))
            .andExpect(jsonPath("$.[*].tipi").value(hasItem(DEFAULT_TIPI)))
            .andExpect(jsonPath("$.[*].cinsi").value(hasItem(DEFAULT_CINSI)))
            .andExpect(jsonPath("$.[*].marka").value(hasItem(DEFAULT_MARKA)))
            .andExpect(jsonPath("$.[*].fiyati").value(hasItem(sameNumber(DEFAULT_FIYATI))));

        // Check, that the count call also returns 1
        restStokMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultStokShouldNotBeFound(String filter) throws Exception {
        restStokMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restStokMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingStok() throws Exception {
        // Get the stok
        restStokMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingStok() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        int databaseSizeBeforeUpdate = stokRepository.findAll().size();

        // Update the stok
        Stok updatedStok = stokRepository.findById(stok.getId()).get();
        // Disconnect from session so that the updates on updatedStok are not directly saved in db
        em.detach(updatedStok);
        updatedStok
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .fiyati(UPDATED_FIYATI);
        StokDTO stokDTO = stokMapper.toDto(updatedStok);

        restStokMockMvc
            .perform(
                put(ENTITY_API_URL_ID, stokDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(stokDTO))
            )
            .andExpect(status().isOk());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
        Stok testStok = stokList.get(stokList.size() - 1);
        assertThat(testStok.getStokno()).isEqualTo(UPDATED_STOKNO);
        assertThat(testStok.getParcano()).isEqualTo(UPDATED_PARCANO);
        assertThat(testStok.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testStok.getCinsi()).isEqualTo(UPDATED_CINSI);
        assertThat(testStok.getMarka()).isEqualTo(UPDATED_MARKA);
        assertThat(testStok.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void putNonExistingStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(
                put(ENTITY_API_URL_ID, stokDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(stokDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(stokDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(stokDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateStokWithPatch() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        int databaseSizeBeforeUpdate = stokRepository.findAll().size();

        // Update the stok using partial update
        Stok partialUpdatedStok = new Stok();
        partialUpdatedStok.setId(stok.getId());

        partialUpdatedStok.parcano(UPDATED_PARCANO).tipi(UPDATED_TIPI).fiyati(UPDATED_FIYATI);

        restStokMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStok.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedStok))
            )
            .andExpect(status().isOk());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
        Stok testStok = stokList.get(stokList.size() - 1);
        assertThat(testStok.getStokno()).isEqualTo(DEFAULT_STOKNO);
        assertThat(testStok.getParcano()).isEqualTo(UPDATED_PARCANO);
        assertThat(testStok.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testStok.getCinsi()).isEqualTo(DEFAULT_CINSI);
        assertThat(testStok.getMarka()).isEqualTo(DEFAULT_MARKA);
        assertThat(testStok.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void fullUpdateStokWithPatch() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        int databaseSizeBeforeUpdate = stokRepository.findAll().size();

        // Update the stok using partial update
        Stok partialUpdatedStok = new Stok();
        partialUpdatedStok.setId(stok.getId());

        partialUpdatedStok
            .stokno(UPDATED_STOKNO)
            .parcano(UPDATED_PARCANO)
            .tipi(UPDATED_TIPI)
            .cinsi(UPDATED_CINSI)
            .marka(UPDATED_MARKA)
            .fiyati(UPDATED_FIYATI);

        restStokMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStok.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedStok))
            )
            .andExpect(status().isOk());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
        Stok testStok = stokList.get(stokList.size() - 1);
        assertThat(testStok.getStokno()).isEqualTo(UPDATED_STOKNO);
        assertThat(testStok.getParcano()).isEqualTo(UPDATED_PARCANO);
        assertThat(testStok.getTipi()).isEqualTo(UPDATED_TIPI);
        assertThat(testStok.getCinsi()).isEqualTo(UPDATED_CINSI);
        assertThat(testStok.getMarka()).isEqualTo(UPDATED_MARKA);
        assertThat(testStok.getFiyati()).isEqualByComparingTo(UPDATED_FIYATI);
    }

    @Test
    @Transactional
    void patchNonExistingStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, stokDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(stokDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(stokDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamStok() throws Exception {
        int databaseSizeBeforeUpdate = stokRepository.findAll().size();
        stok.setId(count.incrementAndGet());

        // Create the Stok
        StokDTO stokDTO = stokMapper.toDto(stok);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStokMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(stokDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Stok in the database
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteStok() throws Exception {
        // Initialize the database
        stokRepository.saveAndFlush(stok);

        int databaseSizeBeforeDelete = stokRepository.findAll().size();

        // Delete the stok
        restStokMockMvc
            .perform(delete(ENTITY_API_URL_ID, stok.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Stok> stokList = stokRepository.findAll();
        assertThat(stokList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
