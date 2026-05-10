package com.circleguard.promotion.service;

import com.circleguard.promotion.exception.FenceException;
import com.circleguard.promotion.model.graph.UserNode;
import com.circleguard.promotion.model.jpa.SystemSettings;
import com.circleguard.promotion.repository.graph.CircleNodeRepository;
import com.circleguard.promotion.repository.graph.UserNodeRepository;
import com.circleguard.promotion.repository.jpa.SystemSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthStatusServiceUnitTest {

    @Mock
    private UserNodeRepository userNodeRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Neo4jClient neo4jClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private SystemSettingsRepository systemSettingsRepository;
    @Mock
    private CircleNodeRepository circleNodeRepository;

    @InjectMocks
    private HealthStatusService healthStatusService;

    @Test
    void getCachedStatus_ReturnsStatusFromRedis() {
        // Arrange
        String anonymousId = "test-user";
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("user:status:" + anonymousId)).thenReturn("ACTIVE");

        // Act
        String status = healthStatusService.getCachedStatus(anonymousId);

        // Assert
        assertEquals("ACTIVE", status);
    }

    @Test
    void updateStatus_Fails_WhenInFenceWindow() {
        // Arrange
        String anonymousId = "fenced-user";
        UserNode user = UserNode.builder()
                .anonymousId(anonymousId)
                .status("SUSPECT")
                .statusUpdatedAt(System.currentTimeMillis() - 1000) // 1 second ago
                .build();
        
        SystemSettings settings = SystemSettings.builder()
                .mandatoryFenceDays(14)
                .build();

        when(userNodeRepository.findById(anonymousId)).thenReturn(Optional.of(user));
        when(systemSettingsRepository.getSettings()).thenReturn(Optional.of(settings));

        // Act & Assert
        assertThrows(FenceException.class, () -> healthStatusService.updateStatus(anonymousId, "ACTIVE", false));
    }

    @Test
    void promoteToRecovered_SetsStatusAndExpiration() {
        // Arrange
        String anonymousId = UUID.randomUUID().toString();
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        
        // Mock Neo4j Client chain for resolveStatus using deep stubs logic
        when(neo4jClient.query(anyString()).bind(any()).to(anyString()).run()).thenReturn(null);

        // Act
        healthStatusService.promoteToRecovered(anonymousId);

        // Assert
        verify(redisTemplate).expire(eq("user:status:" + anonymousId), any());
        verify(valueOps).set(eq("user:status:" + anonymousId), eq("RECOVERED"));
    }

    @Test
    void evictUserCache_DoesNotCrash() {
        // Simple smoke test for programmatic eviction
        assertDoesNotThrow(() -> healthStatusService.evictUserCache("some-id"));
    }

    @Test
    void resolveStatus_UpdatesNeo4j() {
        // Arrange
        String anonymousId = "resolve-user";
        
        // Mock Redis
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        
        // Mock the different parts of the chain called in resolveStatus
        when(neo4jClient.query(anyString()).bind(any()).to(anyString()).run()).thenReturn(null);
        
        var fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
        when(neo4jClient.query(anyString()).bind(any()).to(anyString()).fetch()).thenReturn(fetchSpec);
        when(fetchSpec.one()).thenReturn(Optional.empty());

        // Act
        healthStatusService.resolveStatus(anonymousId, true); // Admin override to skip fence check

        // Assert
        verify(neo4jClient, atLeastOnce()).query(anyString());
    }
}
