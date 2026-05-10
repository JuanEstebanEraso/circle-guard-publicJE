package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenServiceTest {

    private JwtTokenService service;
    private final String secret = "super-secret-key-that-must-be-very-long-for-hs256";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        service = new JwtTokenService(secret, expiration);
    }

    // TEST 1
    @Test
    void shouldGenerateValidTokenWithCorrectClaims() {
        UUID anonymousId = UUID.randomUUID();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
               .when(auth).getAuthorities();

        String token = service.generateToken(anonymousId, auth);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(anonymousId.toString(), claims.getSubject());
        
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) claims.get("permissions");
        assertTrue(permissions.contains("ROLE_STUDENT"));
    }

    // TEST 2
    @Test
    void generatedTokenShouldHaveCorrectExpiration() {
        UUID anonymousId = UUID.randomUUID();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.doReturn(Collections.emptyList()).when(auth).getAuthorities();

        long before = System.currentTimeMillis();
        String token = service.generateToken(anonymousId, auth);
        long after = System.currentTimeMillis();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getExpiration());
        long expTime = claims.getExpiration().getTime();
        
        // Assert expiration is within expected range (1 hour from now)
        assertTrue(expTime >= before + expiration);
        assertTrue(expTime <= after + expiration + 1000); // 1 sec tolerance
    }
}
