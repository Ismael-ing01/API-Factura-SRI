package com.factura.sri.security;

import com.factura.sri.enums.Role;
import com.factura.sri.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails user = Usuario.builder()
                .username("testuser")
                .password("password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void testExtractUsername() {
        UserDetails user = Usuario.builder()
                .username("testuser")
                .password("password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        Assertions.assertEquals("testuser", username);
    }
}
