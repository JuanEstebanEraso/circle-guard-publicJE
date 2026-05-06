package com.circleguard.identity.service;

import com.circleguard.identity.model.IdentityMapping;
import com.circleguard.identity.repository.IdentityMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class IdentityVaultServiceTest {

    private IdentityVaultService service;
    private IdentityMappingRepository repository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(IdentityMappingRepository.class);
        service = new IdentityVaultService(repository);
        ReflectionTestUtils.setField(service, "hashSalt", "test-salt");
    }

    // TEST 3
    @Test
    void shouldCreateNewAnonymousIdIfIdentityNotFound() {
        String realIdentity = "student1@circleguard.edu";
        
        Mockito.when(repository.findByIdentityHash(any(String.class))).thenReturn(Optional.empty());
        
        IdentityMapping savedMapping = IdentityMapping.builder()
                .anonymousId(UUID.randomUUID())
                .realIdentity(realIdentity)
                .build();
                
        Mockito.when(repository.save(any(IdentityMapping.class))).thenReturn(savedMapping);

        UUID resultId = service.getOrCreateAnonymousId(realIdentity);

        assertNotNull(resultId);
        assertEquals(savedMapping.getAnonymousId(), resultId);
        Mockito.verify(repository).save(any(IdentityMapping.class));
    }

    // TEST 4
    @Test
    void shouldThrowExceptionWhenResolvingUnknownIdentity() {
        UUID unknownId = UUID.randomUUID();
        Mockito.when(repository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            service.resolveRealIdentity(unknownId);
        });
    }
}
