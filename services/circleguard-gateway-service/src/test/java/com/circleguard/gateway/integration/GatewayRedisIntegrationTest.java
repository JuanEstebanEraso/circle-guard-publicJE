package com.circleguard.gateway.integration;

import com.circleguard.gateway.service.QrValidationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.Key;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Testcontainers
public class GatewayRedisIntegrationTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>("redis:7.2").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private QrValidationService qrValidationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // INTEGRATION TEST 4: Validates real communication between Gateway and Redis (using Testcontainers)
    @Test
    void gatewayShouldReadStatusFromRealRedis() {
        String secret = "my-super-secret-test-key-32-chars-long"; // Must match application.yml in test context
        String anonymousId = UUID.randomUUID().toString();
        
        // Write to Redis exactly as Promotion Service would
        redisTemplate.opsForValue().set("user:status:" + anonymousId, "CONTAGIED");

        // Generate token
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        String token = Jwts.builder()
                .setSubject(anonymousId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Validate
        QrValidationService.ValidationResult result = qrValidationService.validateToken(token);
        
        assertFalse(result.valid());
        assertEquals("RED", result.status());
    }
}
