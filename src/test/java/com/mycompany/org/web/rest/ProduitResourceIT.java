package com.mycompany.org.web.rest;

import com.mycompany.org.JhipsterSampleApplicationApp;
import com.mycompany.org.domain.Produit;
import com.mycompany.org.domain.Categorie;
import com.mycompany.org.repository.ProduitRepository;
import com.mycompany.org.service.ProduitService;
import com.mycompany.org.service.dto.ProduitDTO;
import com.mycompany.org.service.mapper.ProduitMapper;
import com.mycompany.org.service.dto.ProduitCriteria;
import com.mycompany.org.service.ProduitQueryService;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProduitResource} REST controller.
 */
@SpringBootTest(classes = JhipsterSampleApplicationApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class ProduitResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRIX = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRIX = new BigDecimal(2);
    private static final BigDecimal SMALLER_PRIX = new BigDecimal(1 - 1);

    private static final LocalDate DEFAULT_DATE_CREATION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_CREATION = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DATE_CREATION = LocalDate.ofEpochDay(-1L);

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private ProduitMapper produitMapper;

    @Autowired
    private ProduitService produitService;

    @Autowired
    private ProduitQueryService produitQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProduitMockMvc;

    private Produit produit;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produit createEntity(EntityManager em) {
        Produit produit = new Produit()
            .code(DEFAULT_CODE)
            .description(DEFAULT_DESCRIPTION)
            .prix(DEFAULT_PRIX)
            .dateCreation(DEFAULT_DATE_CREATION);
        return produit;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Produit createUpdatedEntity(EntityManager em) {
        Produit produit = new Produit()
            .code(UPDATED_CODE)
            .description(UPDATED_DESCRIPTION)
            .prix(UPDATED_PRIX)
            .dateCreation(UPDATED_DATE_CREATION);
        return produit;
    }

    @BeforeEach
    public void initTest() {
        produit = createEntity(em);
    }

    @Test
    @Transactional
    public void createProduit() throws Exception {
        int databaseSizeBeforeCreate = produitRepository.findAll().size();
        // Create the Produit
        ProduitDTO produitDTO = produitMapper.toDto(produit);
        restProduitMockMvc.perform(post("/api/produits").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(produitDTO)))
            .andExpect(status().isCreated());

        // Validate the Produit in the database
        List<Produit> produitList = produitRepository.findAll();
        assertThat(produitList).hasSize(databaseSizeBeforeCreate + 1);
        Produit testProduit = produitList.get(produitList.size() - 1);
        assertThat(testProduit.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testProduit.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testProduit.getPrix()).isEqualTo(DEFAULT_PRIX);
        assertThat(testProduit.getDateCreation()).isEqualTo(DEFAULT_DATE_CREATION);
    }

    @Test
    @Transactional
    public void createProduitWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = produitRepository.findAll().size();

        // Create the Produit with an existing ID
        produit.setId(1L);
        ProduitDTO produitDTO = produitMapper.toDto(produit);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProduitMockMvc.perform(post("/api/produits").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(produitDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Produit in the database
        List<Produit> produitList = produitRepository.findAll();
        assertThat(produitList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllProduits() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList
        restProduitMockMvc.perform(get("/api/produits?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(produit.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].prix").value(hasItem(DEFAULT_PRIX.intValue())))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }
    
    @Test
    @Transactional
    public void getProduit() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get the produit
        restProduitMockMvc.perform(get("/api/produits/{id}", produit.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(produit.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.prix").value(DEFAULT_PRIX.intValue()))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }


    @Test
    @Transactional
    public void getProduitsByIdFiltering() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        Long id = produit.getId();

        defaultProduitShouldBeFound("id.equals=" + id);
        defaultProduitShouldNotBeFound("id.notEquals=" + id);

        defaultProduitShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProduitShouldNotBeFound("id.greaterThan=" + id);

        defaultProduitShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProduitShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllProduitsByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code equals to DEFAULT_CODE
        defaultProduitShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the produitList where code equals to UPDATED_CODE
        defaultProduitShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProduitsByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code not equals to DEFAULT_CODE
        defaultProduitShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the produitList where code not equals to UPDATED_CODE
        defaultProduitShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProduitsByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code in DEFAULT_CODE or UPDATED_CODE
        defaultProduitShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the produitList where code equals to UPDATED_CODE
        defaultProduitShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProduitsByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code is not null
        defaultProduitShouldBeFound("code.specified=true");

        // Get all the produitList where code is null
        defaultProduitShouldNotBeFound("code.specified=false");
    }
                @Test
    @Transactional
    public void getAllProduitsByCodeContainsSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code contains DEFAULT_CODE
        defaultProduitShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the produitList where code contains UPDATED_CODE
        defaultProduitShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllProduitsByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where code does not contain DEFAULT_CODE
        defaultProduitShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the produitList where code does not contain UPDATED_CODE
        defaultProduitShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }


    @Test
    @Transactional
    public void getAllProduitsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description equals to DEFAULT_DESCRIPTION
        defaultProduitShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the produitList where description equals to UPDATED_DESCRIPTION
        defaultProduitShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDescriptionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description not equals to DEFAULT_DESCRIPTION
        defaultProduitShouldNotBeFound("description.notEquals=" + DEFAULT_DESCRIPTION);

        // Get all the produitList where description not equals to UPDATED_DESCRIPTION
        defaultProduitShouldBeFound("description.notEquals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultProduitShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the produitList where description equals to UPDATED_DESCRIPTION
        defaultProduitShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description is not null
        defaultProduitShouldBeFound("description.specified=true");

        // Get all the produitList where description is null
        defaultProduitShouldNotBeFound("description.specified=false");
    }
                @Test
    @Transactional
    public void getAllProduitsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description contains DEFAULT_DESCRIPTION
        defaultProduitShouldBeFound("description.contains=" + DEFAULT_DESCRIPTION);

        // Get all the produitList where description contains UPDATED_DESCRIPTION
        defaultProduitShouldNotBeFound("description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where description does not contain DEFAULT_DESCRIPTION
        defaultProduitShouldNotBeFound("description.doesNotContain=" + DEFAULT_DESCRIPTION);

        // Get all the produitList where description does not contain UPDATED_DESCRIPTION
        defaultProduitShouldBeFound("description.doesNotContain=" + UPDATED_DESCRIPTION);
    }


    @Test
    @Transactional
    public void getAllProduitsByPrixIsEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix equals to DEFAULT_PRIX
        defaultProduitShouldBeFound("prix.equals=" + DEFAULT_PRIX);

        // Get all the produitList where prix equals to UPDATED_PRIX
        defaultProduitShouldNotBeFound("prix.equals=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsNotEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix not equals to DEFAULT_PRIX
        defaultProduitShouldNotBeFound("prix.notEquals=" + DEFAULT_PRIX);

        // Get all the produitList where prix not equals to UPDATED_PRIX
        defaultProduitShouldBeFound("prix.notEquals=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsInShouldWork() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix in DEFAULT_PRIX or UPDATED_PRIX
        defaultProduitShouldBeFound("prix.in=" + DEFAULT_PRIX + "," + UPDATED_PRIX);

        // Get all the produitList where prix equals to UPDATED_PRIX
        defaultProduitShouldNotBeFound("prix.in=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsNullOrNotNull() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix is not null
        defaultProduitShouldBeFound("prix.specified=true");

        // Get all the produitList where prix is null
        defaultProduitShouldNotBeFound("prix.specified=false");
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix is greater than or equal to DEFAULT_PRIX
        defaultProduitShouldBeFound("prix.greaterThanOrEqual=" + DEFAULT_PRIX);

        // Get all the produitList where prix is greater than or equal to UPDATED_PRIX
        defaultProduitShouldNotBeFound("prix.greaterThanOrEqual=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix is less than or equal to DEFAULT_PRIX
        defaultProduitShouldBeFound("prix.lessThanOrEqual=" + DEFAULT_PRIX);

        // Get all the produitList where prix is less than or equal to SMALLER_PRIX
        defaultProduitShouldNotBeFound("prix.lessThanOrEqual=" + SMALLER_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsLessThanSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix is less than DEFAULT_PRIX
        defaultProduitShouldNotBeFound("prix.lessThan=" + DEFAULT_PRIX);

        // Get all the produitList where prix is less than UPDATED_PRIX
        defaultProduitShouldBeFound("prix.lessThan=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    public void getAllProduitsByPrixIsGreaterThanSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where prix is greater than DEFAULT_PRIX
        defaultProduitShouldNotBeFound("prix.greaterThan=" + DEFAULT_PRIX);

        // Get all the produitList where prix is greater than SMALLER_PRIX
        defaultProduitShouldBeFound("prix.greaterThan=" + SMALLER_PRIX);
    }


    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation equals to DEFAULT_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.equals=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation equals to UPDATED_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.equals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation not equals to DEFAULT_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.notEquals=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation not equals to UPDATED_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.notEquals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsInShouldWork() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation in DEFAULT_DATE_CREATION or UPDATED_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.in=" + DEFAULT_DATE_CREATION + "," + UPDATED_DATE_CREATION);

        // Get all the produitList where dateCreation equals to UPDATED_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.in=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsNullOrNotNull() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation is not null
        defaultProduitShouldBeFound("dateCreation.specified=true");

        // Get all the produitList where dateCreation is null
        defaultProduitShouldNotBeFound("dateCreation.specified=false");
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation is greater than or equal to DEFAULT_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.greaterThanOrEqual=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation is greater than or equal to UPDATED_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.greaterThanOrEqual=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation is less than or equal to DEFAULT_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.lessThanOrEqual=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation is less than or equal to SMALLER_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.lessThanOrEqual=" + SMALLER_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsLessThanSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation is less than DEFAULT_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.lessThan=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation is less than UPDATED_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.lessThan=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void getAllProduitsByDateCreationIsGreaterThanSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        // Get all the produitList where dateCreation is greater than DEFAULT_DATE_CREATION
        defaultProduitShouldNotBeFound("dateCreation.greaterThan=" + DEFAULT_DATE_CREATION);

        // Get all the produitList where dateCreation is greater than SMALLER_DATE_CREATION
        defaultProduitShouldBeFound("dateCreation.greaterThan=" + SMALLER_DATE_CREATION);
    }


    @Test
    @Transactional
    public void getAllProduitsByCategorieIsEqualToSomething() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);
        Categorie categorie = CategorieResourceIT.createEntity(em);
        em.persist(categorie);
        em.flush();
        produit.setCategorie(categorie);
        produitRepository.saveAndFlush(produit);
        Long categorieId = categorie.getId();

        // Get all the produitList where categorie equals to categorieId
        defaultProduitShouldBeFound("categorieId.equals=" + categorieId);

        // Get all the produitList where categorie equals to categorieId + 1
        defaultProduitShouldNotBeFound("categorieId.equals=" + (categorieId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProduitShouldBeFound(String filter) throws Exception {
        restProduitMockMvc.perform(get("/api/produits?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(produit.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].prix").value(hasItem(DEFAULT_PRIX.intValue())))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));

        // Check, that the count call also returns 1
        restProduitMockMvc.perform(get("/api/produits/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProduitShouldNotBeFound(String filter) throws Exception {
        restProduitMockMvc.perform(get("/api/produits?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProduitMockMvc.perform(get("/api/produits/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingProduit() throws Exception {
        // Get the produit
        restProduitMockMvc.perform(get("/api/produits/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProduit() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        int databaseSizeBeforeUpdate = produitRepository.findAll().size();

        // Update the produit
        Produit updatedProduit = produitRepository.findById(produit.getId()).get();
        // Disconnect from session so that the updates on updatedProduit are not directly saved in db
        em.detach(updatedProduit);
        updatedProduit
            .code(UPDATED_CODE)
            .description(UPDATED_DESCRIPTION)
            .prix(UPDATED_PRIX)
            .dateCreation(UPDATED_DATE_CREATION);
        ProduitDTO produitDTO = produitMapper.toDto(updatedProduit);

        restProduitMockMvc.perform(put("/api/produits").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(produitDTO)))
            .andExpect(status().isOk());

        // Validate the Produit in the database
        List<Produit> produitList = produitRepository.findAll();
        assertThat(produitList).hasSize(databaseSizeBeforeUpdate);
        Produit testProduit = produitList.get(produitList.size() - 1);
        assertThat(testProduit.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testProduit.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testProduit.getPrix()).isEqualTo(UPDATED_PRIX);
        assertThat(testProduit.getDateCreation()).isEqualTo(UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    public void updateNonExistingProduit() throws Exception {
        int databaseSizeBeforeUpdate = produitRepository.findAll().size();

        // Create the Produit
        ProduitDTO produitDTO = produitMapper.toDto(produit);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProduitMockMvc.perform(put("/api/produits").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(produitDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Produit in the database
        List<Produit> produitList = produitRepository.findAll();
        assertThat(produitList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProduit() throws Exception {
        // Initialize the database
        produitRepository.saveAndFlush(produit);

        int databaseSizeBeforeDelete = produitRepository.findAll().size();

        // Delete the produit
        restProduitMockMvc.perform(delete("/api/produits/{id}", produit.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Produit> produitList = produitRepository.findAll();
        assertThat(produitList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
