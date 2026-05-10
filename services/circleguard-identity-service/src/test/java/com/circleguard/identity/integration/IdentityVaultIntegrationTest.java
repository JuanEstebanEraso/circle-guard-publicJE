package com.circleguard.identity.integration;

import com.circleguard.identity.model.IdentityMapping;
import com.circleguard.identity.repository.IdentityMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IdentityVaultIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private IdentityMappingRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    // INTEGRATION TEST 1: End-to-end flow from API to Database for mapping
    @Test
    @WithMockUser(authorities = "identity:map")
    void whenPostMapIdentity_thenMappingIsCreatedInDb() throws Exception {
        String email = "integration@example.com";

        mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realIdentity\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").exists());

        assertEquals(1, repository.count());
        assertTrue(repository.findByIdentityHash(repository.findAll().get(0).getIdentityHash()).isPresent());
    }

    // INTEGRATION TEST 2: Validates consistency between multiple calls
    @Test
    @WithMockUser(authorities = "identity:map")
    void whenMappingSameIdentityTwice_thenSameIdIsReturned() throws Exception {
        String email = "repeat@example.com";

        String firstResponse = mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realIdentity\":\"" + email + "\"}"))
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realIdentity\":\"" + email + "\"}"))
                .andReturn().getResponse().getContentAsString();

        assertEquals(firstResponse, secondResponse);
        assertEquals(1, repository.count());
    }

    // INTEGRATION TEST 3: Lookup flow from API through Service to DB
    @Test
    @WithMockUser(authorities = "identity:lookup")
    void whenLookupExistingId_thenRealIdentityIsReturnedFromDb() throws Exception {
        // Prepare DB
        IdentityMapping mapping = IdentityMapping.builder()
                .realIdentity("secret@boss.com")
                .identityHash("fake-hash")
                .salt("salt")
                .build();
        mapping = repository.save(mapping);
        UUID anonymousId = mapping.getAnonymousId();

        mockMvc.perform(get("/api/v1/identities/lookup/{id}", anonymousId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realIdentity").value("secret@boss.com"));
    }

    // INTEGRATION TEST 4: Security integration (403 Forbidden)
    @Test
    @WithMockUser(authorities = "wrong:permission")
    void whenLookupWithoutPermission_thenForbiddenIsReturned() throws Exception {
        mockMvc.perform(get("/api/v1/identities/lookup/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // INTEGRATION TEST 5: Data Integrity (Mapping Visitor)
    @Test
    void whenVisitorRequest_thenAnonymousIdIsGeneratedAndStored() throws Exception {
        mockMvc.perform(post("/api/v1/identities/visitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").exists());

        assertEquals(1, repository.count());
        assertNotNull(repository.findAll().get(0).getAnonymousId());
    }
}
