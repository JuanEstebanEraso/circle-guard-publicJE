package com.circleguard.identity.integration;

import com.circleguard.identity.model.IdentityMapping;
import com.circleguard.identity.repository.IdentityMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class IdentityPostgresIntegrationTest {

    @Autowired
    private IdentityMappingRepository repository;

    // INTEGRATION TEST 3: Validates JPA integration with underlying database
    @Test
    void shouldSaveAndRetrieveIdentityMapping() {
        String realIdentity = "student_test@university.edu";
        String hash = "a1b2c3d4e5f6";
        
        IdentityMapping mapping = IdentityMapping.builder()
                .realIdentity(realIdentity)
                .identityHash(hash)
                .salt("salt123")
                .build();
                
        IdentityMapping saved = repository.save(mapping);
        assertNotNull(saved.getAnonymousId());
        
        Optional<IdentityMapping> retrieved = repository.findByIdentityHash(hash);
        assertTrue(retrieved.isPresent());
        assertEquals(realIdentity, retrieved.get().getRealIdentity());
    }
}
