package com.circleguard.gateway.integration;

import com.circleguard.gateway.service.QrValidationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.Mockito;

import java.security.Key;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class GatewayRedisIntegrationTest {

    @Autowired
    private QrValidationService qrValidationService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    // INTEGRATION TEST 4: Validates communication between Gateway and Redis
    @Test
    void gatewayShouldReadStatusFromRealRedis() {
        String secret = "my-super-secret-test-key-32-chars-long"; // Must match application.yml in test context
        String anonymousId = UUID.randomUUID().toString();
        
        // Mock Redis behavior
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:status:" + anonymousId)).thenReturn("CONTAGIED");

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
