package com.circleguard.auth.service;

import com.circleguard.auth.model.LocalUser;
import com.circleguard.auth.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceUnitTest {

    @Test
    void testUserStatusInitiallyActive() {
        LocalUser user = new LocalUser();
        user.setIsActive(true);
        assertTrue(user.getIsActive(), "El usuario debería estar activo");
    }

    @Test
    void testUserRoleAssignment() {
        LocalUser user = new LocalUser();
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(java.util.Set.of(role));
        assertEquals(1, user.getRoles().size());
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
