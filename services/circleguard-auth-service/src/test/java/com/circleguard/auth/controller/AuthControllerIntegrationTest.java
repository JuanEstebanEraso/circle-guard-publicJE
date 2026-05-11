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
        // Probamos que el endpoint de login existe y responde (aunque sea con 401 por falta de credenciales)
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", null, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "El login debería requerir credenciales");
    }

    @Test
    void testPublicHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El endpoint de salud debe ser público");
    }

    @Test
    void testSignupEndpointAvailability() {
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", null, String.class);
        // Esperamos 400 Bad Request porque enviamos un body vacío, lo que indica que el endpoint está ahí
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testInvalidTokenValidation() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/validate?token=invalid", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
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
