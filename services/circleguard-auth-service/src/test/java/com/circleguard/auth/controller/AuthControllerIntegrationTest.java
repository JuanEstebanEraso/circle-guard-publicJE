package com.circleguard.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testLoginEndpointStatus() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", null, String.class);
        assertTrue(!response.getStatusCode().is5xxServerError());
    }

    @Test
    void testPublicHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertTrue(!response.getStatusCode().is5xxServerError());
    }

    @Test
    void testSignupEndpointAvailability() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", null, String.class);
        assertTrue(!response.getStatusCode().is5xxServerError());
    }

    @Test
    void testInvalidTokenValidation() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/validate?token=invalid", String.class);
        assertTrue(!response.getStatusCode().is5xxServerError());
    }

    @Test
    void testRootEndpointRedirectOr404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        // Verificamos que el servidor responde algo (no importa si es 404), indicando que está arriba
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is2xxSuccessful());
    }
    
    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
