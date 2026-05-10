package com.circleguard.promotion.integration;

import com.circleguard.promotion.service.HealthStatusService;
import com.circleguard.promotion.repository.graph.UserNodeRepository;
import com.circleguard.promotion.repository.graph.CircleNodeRepository;
import com.circleguard.promotion.repository.jpa.SystemSettingsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@SpringBootTest
@DisabledIfEnvironmentVariable(named = "JENKINS_HOME", matches = ".*")
public class PromotionKafkaIntegrationTest {

    @Autowired
    private HealthStatusService healthStatusService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private Neo4jClient neo4jClient;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private UserNodeRepository userNodeRepository;

    @MockBean
    private CircleNodeRepository circleNodeRepository;

    @MockBean
    private SystemSettingsRepository systemSettingsRepository;

    // INTEGRATION TEST 1: Validates that Service layer integrates correctly with Kafka messaging layer
    @Test
    void whenStatusChanges_thenKafkaMessageIsSent() {
        String anonymousId = UUID.randomUUID().toString();
        
        // Mock Neo4j Client behavior using Deep Stubs to avoid complex generic type issues
        Neo4jClient.RecordFetchSpec fetchSpec = Mockito.mock(Neo4jClient.RecordFetchSpec.class);
        
        Mockito.when(neo4jClient.query(anyString())
                .bind(any()).to(anyString())
                .bind(any()).to(anyString())
                .bind(any()).to(anyString())
                .fetch()).thenReturn(fetchSpec);
        
        // Simulate finding the node
        Mockito.when(fetchSpec.one()).thenReturn(Optional.of(Map.of("sourceId", anonymousId)));

        // Mock Redis
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Execute service method
        healthStatusService.updateStatus(anonymousId, "CONFIRMED", true);

        // Verify Integration: KafkaTemplate should have been called
        verify(kafkaTemplate).send(
            eq("promotion.status.changed"),
            eq(anonymousId),
            any(Map.class)
        );
    }
}
