package com.dizel.fikopar.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dizel.fikopar.IntegrationTest;
import com.dizel.fikopar.domain.Katalog;
import com.dizel.fikopar.domain.Stok;
import com.dizel.fikopar.repository.KatalogRepository;
import com.dizel.fikopar.service.criteria.KatalogCriteria;
import com.dizel.fikopar.service.dto.KatalogDTO;
import com.dizel.fikopar.service.mapper.KatalogMapper;
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
 * Integration tests for the {@link KatalogResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class KatalogResourceIT {

    private static final String DEFAULT_KATALOGADI = "AAAAAAAAAA";
    private static final String UPDATED_KATALOGADI = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/katalogs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private KatalogRepository katalogRepository;

    @Autowired
    private KatalogMapper katalogMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restKatalogMockMvc;

    private Katalog katalog;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Katalog createEntity(EntityManager em) {
        Katalog katalog = new Katalog().katalogadi(DEFAULT_KATALOGADI);
        return katalog;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Katalog createUpdatedEntity(EntityManager em) {
        Katalog katalog = new Katalog().katalogadi(UPDATED_KATALOGADI);
        return katalog;
    }

    @BeforeEach
    public void initTest() {
        katalog = createEntity(em);
    }

    @Test
    @Transactional
    void createKatalog() throws Exception {
        int databaseSizeBeforeCreate = katalogRepository.findAll().size();
        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);
        restKatalogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(katalogDTO)))
            .andExpect(status().isCreated());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeCreate + 1);
        Katalog testKatalog = katalogList.get(katalogList.size() - 1);
        assertThat(testKatalog.getKatalogadi()).isEqualTo(DEFAULT_KATALOGADI);
    }

    @Test
    @Transactional
    void createKatalogWithExistingId() throws Exception {
        // Create the Katalog with an existing ID
        katalog.setId(1L);
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        int databaseSizeBeforeCreate = katalogRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restKatalogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(katalogDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllKatalogs() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(katalog.getId().intValue())))
            .andExpect(jsonPath("$.[*].katalogadi").value(hasItem(DEFAULT_KATALOGADI)));
    }

    @Test
    @Transactional
    void getKatalog() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get the katalog
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL_ID, katalog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(katalog.getId().intValue()))
            .andExpect(jsonPath("$.katalogadi").value(DEFAULT_KATALOGADI));
    }

    @Test
    @Transactional
    void getKatalogsByIdFiltering() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        Long id = katalog.getId();

        defaultKatalogShouldBeFound("id.equals=" + id);
        defaultKatalogShouldNotBeFound("id.notEquals=" + id);

        defaultKatalogShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultKatalogShouldNotBeFound("id.greaterThan=" + id);

        defaultKatalogShouldBeFound("id.lessThanOrEqual=" + id);
        defaultKatalogShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllKatalogsByKatalogadiIsEqualToSomething() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList where katalogadi equals to DEFAULT_KATALOGADI
        defaultKatalogShouldBeFound("katalogadi.equals=" + DEFAULT_KATALOGADI);

        // Get all the katalogList where katalogadi equals to UPDATED_KATALOGADI
        defaultKatalogShouldNotBeFound("katalogadi.equals=" + UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void getAllKatalogsByKatalogadiIsInShouldWork() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList where katalogadi in DEFAULT_KATALOGADI or UPDATED_KATALOGADI
        defaultKatalogShouldBeFound("katalogadi.in=" + DEFAULT_KATALOGADI + "," + UPDATED_KATALOGADI);

        // Get all the katalogList where katalogadi equals to UPDATED_KATALOGADI
        defaultKatalogShouldNotBeFound("katalogadi.in=" + UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void getAllKatalogsByKatalogadiIsNullOrNotNull() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList where katalogadi is not null
        defaultKatalogShouldBeFound("katalogadi.specified=true");

        // Get all the katalogList where katalogadi is null
        defaultKatalogShouldNotBeFound("katalogadi.specified=false");
    }

    @Test
    @Transactional
    void getAllKatalogsByKatalogadiContainsSomething() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList where katalogadi contains DEFAULT_KATALOGADI
        defaultKatalogShouldBeFound("katalogadi.contains=" + DEFAULT_KATALOGADI);

        // Get all the katalogList where katalogadi contains UPDATED_KATALOGADI
        defaultKatalogShouldNotBeFound("katalogadi.contains=" + UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void getAllKatalogsByKatalogadiNotContainsSomething() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        // Get all the katalogList where katalogadi does not contain DEFAULT_KATALOGADI
        defaultKatalogShouldNotBeFound("katalogadi.doesNotContain=" + DEFAULT_KATALOGADI);

        // Get all the katalogList where katalogadi does not contain UPDATED_KATALOGADI
        defaultKatalogShouldBeFound("katalogadi.doesNotContain=" + UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void getAllKatalogsByStoksIsEqualToSomething() throws Exception {
        Stok stoks;
        if (TestUtil.findAll(em, Stok.class).isEmpty()) {
            katalogRepository.saveAndFlush(katalog);
            stoks = StokResourceIT.createEntity(em);
        } else {
            stoks = TestUtil.findAll(em, Stok.class).get(0);
        }
        em.persist(stoks);
        em.flush();
        katalog.addStoks(stoks);
        katalogRepository.saveAndFlush(katalog);
        Long stoksId = stoks.getId();

        // Get all the katalogList where stoks equals to stoksId
        defaultKatalogShouldBeFound("stoksId.equals=" + stoksId);

        // Get all the katalogList where stoks equals to (stoksId + 1)
        defaultKatalogShouldNotBeFound("stoksId.equals=" + (stoksId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultKatalogShouldBeFound(String filter) throws Exception {
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(katalog.getId().intValue())))
            .andExpect(jsonPath("$.[*].katalogadi").value(hasItem(DEFAULT_KATALOGADI)));

        // Check, that the count call also returns 1
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultKatalogShouldNotBeFound(String filter) throws Exception {
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restKatalogMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingKatalog() throws Exception {
        // Get the katalog
        restKatalogMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingKatalog() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();

        // Update the katalog
        Katalog updatedKatalog = katalogRepository.findById(katalog.getId()).get();
        // Disconnect from session so that the updates on updatedKatalog are not directly saved in db
        em.detach(updatedKatalog);
        updatedKatalog.katalogadi(UPDATED_KATALOGADI);
        KatalogDTO katalogDTO = katalogMapper.toDto(updatedKatalog);

        restKatalogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, katalogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isOk());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
        Katalog testKatalog = katalogList.get(katalogList.size() - 1);
        assertThat(testKatalog.getKatalogadi()).isEqualTo(UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void putNonExistingKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, katalogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(katalogDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateKatalogWithPatch() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();

        // Update the katalog using partial update
        Katalog partialUpdatedKatalog = new Katalog();
        partialUpdatedKatalog.setId(katalog.getId());

        restKatalogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedKatalog.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedKatalog))
            )
            .andExpect(status().isOk());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
        Katalog testKatalog = katalogList.get(katalogList.size() - 1);
        assertThat(testKatalog.getKatalogadi()).isEqualTo(DEFAULT_KATALOGADI);
    }

    @Test
    @Transactional
    void fullUpdateKatalogWithPatch() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();

        // Update the katalog using partial update
        Katalog partialUpdatedKatalog = new Katalog();
        partialUpdatedKatalog.setId(katalog.getId());

        partialUpdatedKatalog.katalogadi(UPDATED_KATALOGADI);

        restKatalogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedKatalog.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedKatalog))
            )
            .andExpect(status().isOk());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
        Katalog testKatalog = katalogList.get(katalogList.size() - 1);
        assertThat(testKatalog.getKatalogadi()).isEqualTo(UPDATED_KATALOGADI);
    }

    @Test
    @Transactional
    void patchNonExistingKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, katalogDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamKatalog() throws Exception {
        int databaseSizeBeforeUpdate = katalogRepository.findAll().size();
        katalog.setId(count.incrementAndGet());

        // Create the Katalog
        KatalogDTO katalogDTO = katalogMapper.toDto(katalog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restKatalogMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(katalogDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Katalog in the database
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteKatalog() throws Exception {
        // Initialize the database
        katalogRepository.saveAndFlush(katalog);

        int databaseSizeBeforeDelete = katalogRepository.findAll().size();

        // Delete the katalog
        restKatalogMockMvc
            .perform(delete(ENTITY_API_URL_ID, katalog.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Katalog> katalogList = katalogRepository.findAll();
        assertThat(katalogList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
