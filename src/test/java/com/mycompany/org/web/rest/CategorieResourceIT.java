package com.mycompany.org.web.rest;

import com.mycompany.org.JhipsterSampleApplicationApp;
import com.mycompany.org.domain.Categorie;
import com.mycompany.org.domain.Produit;
import com.mycompany.org.repository.CategorieRepository;
import com.mycompany.org.service.CategorieService;
import com.mycompany.org.service.dto.CategorieDTO;
import com.mycompany.org.service.mapper.CategorieMapper;
import com.mycompany.org.service.dto.CategorieCriteria;
import com.mycompany.org.service.CategorieQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CategorieResource} REST controller.
 */
@SpringBootTest(classes = JhipsterSampleApplicationApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CategorieResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private CategorieMapper categorieMapper;

    @Autowired
    private CategorieService categorieService;

    @Autowired
    private CategorieQueryService categorieQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCategorieMockMvc;

    private Categorie categorie;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Categorie createEntity(EntityManager em) {
        Categorie categorie = new Categorie()
            .code(DEFAULT_CODE)
            .description(DEFAULT_DESCRIPTION);
        return categorie;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Categorie createUpdatedEntity(EntityManager em) {
        Categorie categorie = new Categorie()
            .code(UPDATED_CODE)
            .description(UPDATED_DESCRIPTION);
        return categorie;
    }

    @BeforeEach
    public void initTest() {
        categorie = createEntity(em);
    }

    @Test
    @Transactional
    public void createCategorie() throws Exception {
        int databaseSizeBeforeCreate = categorieRepository.findAll().size();
        // Create the Categorie
        CategorieDTO categorieDTO = categorieMapper.toDto(categorie);
        restCategorieMockMvc.perform(post("/api/categories").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categorieDTO)))
            .andExpect(status().isCreated());

        // Validate the Categorie in the database
        List<Categorie> categorieList = categorieRepository.findAll();
        assertThat(categorieList).hasSize(databaseSizeBeforeCreate + 1);
        Categorie testCategorie = categorieList.get(categorieList.size() - 1);
        assertThat(testCategorie.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testCategorie.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    public void createCategorieWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = categorieRepository.findAll().size();

        // Create the Categorie with an existing ID
        categorie.setId(1L);
        CategorieDTO categorieDTO = categorieMapper.toDto(categorie);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategorieMockMvc.perform(post("/api/categories").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categorieDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Categorie in the database
        List<Categorie> categorieList = categorieRepository.findAll();
        assertThat(categorieList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCategories() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList
        restCategorieMockMvc.perform(get("/api/categories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(categorie.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
    
    @Test
    @Transactional
    public void getCategorie() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get the categorie
        restCategorieMockMvc.perform(get("/api/categories/{id}", categorie.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(categorie.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }


    @Test
    @Transactional
    public void getCategoriesByIdFiltering() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        Long id = categorie.getId();

        defaultCategorieShouldBeFound("id.equals=" + id);
        defaultCategorieShouldNotBeFound("id.notEquals=" + id);

        defaultCategorieShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCategorieShouldNotBeFound("id.greaterThan=" + id);

        defaultCategorieShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCategorieShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllCategoriesByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code equals to DEFAULT_CODE
        defaultCategorieShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the categorieList where code equals to UPDATED_CODE
        defaultCategorieShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code not equals to DEFAULT_CODE
        defaultCategorieShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the categorieList where code not equals to UPDATED_CODE
        defaultCategorieShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code in DEFAULT_CODE or UPDATED_CODE
        defaultCategorieShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the categorieList where code equals to UPDATED_CODE
        defaultCategorieShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code is not null
        defaultCategorieShouldBeFound("code.specified=true");

        // Get all the categorieList where code is null
        defaultCategorieShouldNotBeFound("code.specified=false");
    }
                @Test
    @Transactional
    public void getAllCategoriesByCodeContainsSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code contains DEFAULT_CODE
        defaultCategorieShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the categorieList where code contains UPDATED_CODE
        defaultCategorieShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllCategoriesByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where code does not contain DEFAULT_CODE
        defaultCategorieShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the categorieList where code does not contain UPDATED_CODE
        defaultCategorieShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }


    @Test
    @Transactional
    public void getAllCategoriesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description equals to DEFAULT_DESCRIPTION
        defaultCategorieShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the categorieList where description equals to UPDATED_DESCRIPTION
        defaultCategorieShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description not equals to DEFAULT_DESCRIPTION
        defaultCategorieShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the categorieList where description not equals to UPDATED_DESCRIPTION
        defaultCategorieShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultCategorieShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the categorieList where description equals to UPDATED_DESCRIPTION
        defaultCategorieShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description is not null
        defaultCategorieShouldBeFound("description.specified=true");

        // Get all the categorieList where description is null
        defaultCategorieShouldNotBeFound("description.specified=false");
    }
                @Test
    @Transactional
    public void getAllCategoriesByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description contains DEFAULT_DESCRIPTION
        defaultCategorieShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the categorieList where description contains UPDATED_DESCRIPTION
        defaultCategorieShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCategoriesByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        // Get all the categorieList where description does not contain DEFAULT_DESCRIPTION
        defaultCategorieShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the categorieList where description does not contain UPDATED_DESCRIPTION
        defaultCategorieShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }


    @Test
    @Transactional
    public void getAllCategoriesByProduitIsEqualToSomething() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);
        Produit produit = ProduitResourceIT.createEntity(em);
        em.persist(produit);
        em.flush();
        categorie.addProduit(produit);
        categorieRepository.saveAndFlush(categorie);
        Long produitId = produit.getId();

        // Get all the categorieList where produit equals to produitId
        defaultCategorieShouldBeFound("produitId.equals=" + produitId);

        // Get all the categorieList where produit equals to produitId + 1
        defaultCategorieShouldNotBeFound("produitId.equals=" + (produitId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCategorieShouldBeFound(String filter) throws Exception {
        restCategorieMockMvc.perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(categorie.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restCategorieMockMvc.perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCategorieShouldNotBeFound(String filter) throws Exception {
        restCategorieMockMvc.perform(get("/api/categories?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCategorieMockMvc.perform(get("/api/categories/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCategorie() throws Exception {
        // Get the categorie
        restCategorieMockMvc.perform(get("/api/categories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCategorie() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        int databaseSizeBeforeUpdate = categorieRepository.findAll().size();

        // Update the categorie
        Categorie updatedCategorie = categorieRepository.findById(categorie.getId()).get();
        // Disconnect from session so that the updates on updatedCategorie are not directly saved in db
        em.detach(updatedCategorie);
        updatedCategorie
            .code(UPDATED_CODE)
            .description(UPDATED_DESCRIPTION);
        CategorieDTO categorieDTO = categorieMapper.toDto(updatedCategorie);

        restCategorieMockMvc.perform(put("/api/categories").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categorieDTO)))
            .andExpect(status().isOk());

        // Validate the Categorie in the database
        List<Categorie> categorieList = categorieRepository.findAll();
        assertThat(categorieList).hasSize(databaseSizeBeforeUpdate);
        Categorie testCategorie = categorieList.get(categorieList.size() - 1);
        assertThat(testCategorie.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testCategorie.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void updateNonExistingCategorie() throws Exception {
        int databaseSizeBeforeUpdate = categorieRepository.findAll().size();

        // Create the Categorie
        CategorieDTO categorieDTO = categorieMapper.toDto(categorie);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategorieMockMvc.perform(put("/api/categories").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(categorieDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Categorie in the database
        List<Categorie> categorieList = categorieRepository.findAll();
        assertThat(categorieList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCategorie() throws Exception {
        // Initialize the database
        categorieRepository.saveAndFlush(categorie);

        int databaseSizeBeforeDelete = categorieRepository.findAll().size();

        // Delete the categorie
        restCategorieMockMvc.perform(delete("/api/categories/{id}", categorie.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Categorie> categorieList = categorieRepository.findAll();
        assertThat(categorieList).hasSize(databaseSizeBeforeDelete - 1);
    }
}