package com.circleguard.identity.service;

import com.circleguard.identity.model.IdentityMapping;
import com.circleguard.identity.repository.IdentityMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityVaultServiceUnitTest {

    @Mock
    private IdentityMappingRepository repository;

    @InjectMocks
    private IdentityVaultService vaultService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vaultService, "hashSalt", "test-salt");
    }

    @Test
    void getOrCreateAnonymousId_ReturnsExistingId_WhenHashMatches() {
        // Arrange
        String email = "test@example.com";
        UUID existingUuid = UUID.randomUUID();
        IdentityMapping mapping = IdentityMapping.builder()
                .anonymousId(existingUuid)
                .realIdentity(email)
                .build();
        
        when(repository.findByIdentityHash(anyString())).thenReturn(Optional.of(mapping));

        // Act
        UUID result = vaultService.getOrCreateAnonymousId(email);

        // Assert
        assertEquals(existingUuid, result);
        verify(repository, never()).save(any());
    }

    @Test
    void getOrCreateAnonymousId_CreatesNewId_WhenHashIsNew() {
        // Arrange
        String email = "new@example.com";
        UUID newUuid = UUID.randomUUID();
        when(repository.findByIdentityHash(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(IdentityMapping.class))).thenAnswer(invocation -> {
            IdentityMapping saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "anonymousId", newUuid);
            return saved;
        });

        // Act
        UUID result = vaultService.getOrCreateAnonymousId(email);

        // Assert
        assertEquals(newUuid, result);
        verify(repository, times(1)).save(any());
    }

    @Test
    void resolveRealIdentity_ReturnsEmail_WhenIdExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        String email = "found@example.com";
        IdentityMapping mapping = IdentityMapping.builder()
                .anonymousId(id)
                .realIdentity(email)
                .build();
        
        when(repository.findById(id)).thenReturn(Optional.of(mapping));

        // Act
        String result = vaultService.resolveRealIdentity(id);

        // Assert
        assertEquals(email, result);
    }

    @Test
    void resolveRealIdentity_ThrowsNotFound_WhenIdMissing() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> vaultService.resolveRealIdentity(id));
    }

    @Test
    void computeHash_IsConsistent_ForSameInput() {
        // This tests the private method indirectly via getOrCreateAnonymousId
        String email = "consistent@example.com";
        
        // We'll capture the hash sent to the repository
        when(repository.findByIdentityHash(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(IdentityMapping.builder().anonymousId(UUID.randomUUID()).build());

        vaultService.getOrCreateAnonymousId(email);
        
        // Get the hash from the first call
        var hashCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).findByIdentityHash(hashCaptor.capture());
        String firstHash = hashCaptor.getValue();

        // Call again
        vaultService.getOrCreateAnonymousId(email);
        verify(repository, times(2)).findByIdentityHash(hashCaptor.capture());
        String secondHash = hashCaptor.getAllValues().get(1);

        assertEquals(firstHash, secondHash, "Hash should be consistent for the same email and salt");
    }
}
