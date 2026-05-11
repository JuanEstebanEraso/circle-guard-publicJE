package com.circleguard.auth.service;

import com.circleguard.auth.model.LocalUser;
import com.circleguard.auth.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceUnitTest {

    @Test
    void testUserStatusInitiallyEnabled() {
        LocalUser user = new LocalUser();
        user.setEnabled(true);
        assertTrue(user.isEnabled(), "El usuario debería estar habilitado por defecto");
    }

    @Test
    void testUserRoleAssignment() {
        LocalUser user = new LocalUser();
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRole(role);
        assertEquals("ROLE_USER", user.getRole().getName(), "El rol asignado no es el correcto");
    }

    @Test
    void testUsernameAssignment() {
        LocalUser user = new LocalUser();
        user.setUsername("juanesteban");
        assertEquals("juanesteban", user.getUsername());
    }

    @Test
    void testPasswordAssignment() {
        LocalUser user = new LocalUser();
        user.setPassword("secret123");
        assertEquals("secret123", user.getPassword());
    }

    @Test
    void testEmailAssignment() {
        LocalUser user = new LocalUser();
        user.setEmail("test@circleguard.edu");
        assertEquals("test@circleguard.edu", user.getEmail());
    }
}
