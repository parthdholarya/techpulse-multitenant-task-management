
package com.techpulse.taskmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Test-only Base64 secret
    private static final String TEST_SECRET =
            "VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JUZXN0aW5nMTIzNDU2";

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService, "secret", TEST_SECRET
        );

        ReflectionTestUtils.setField(
                jwtService, "expiration", 86400000L
        );
    }

    @Test
    void generateTokenSuccessfully() {

        String token = jwtService.generateToken(
                "user@techpulse.com",
                1L,
                "USER"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmailSuccessfully() {

        String token = jwtService.generateToken(
                "user@techpulse.com",
                1L,
                "USER"
        );

        String email = jwtService.extractEmail(token);

        assertEquals("user@techpulse.com", email);
    }

    @Test
    void extractTenantIdSuccessfully() {

        String token = jwtService.generateToken(
                "user@techpulse.com",
                1L,
                "USER"
        );

        Long tenantId = jwtService.extractTenantId(token);

        assertEquals(1L, tenantId);
    }

    @Test
    void extractRoleSuccessfully() {

        String token = jwtService.generateToken(
                "manager@techpulse.com",
                2L,
                "MANAGER"
        );

        String role = jwtService.extractRole(token);

        assertEquals("MANAGER", role);
    }

    @Test
    void extractAllClaimsSuccessfully() {

        String token = jwtService.generateToken(
                "admin@techpulse.com",
                3L,
                "ADMIN"
        );

        Claims claims = jwtService.extractClaims(token);

        assertEquals("admin@techpulse.com", claims.getSubject());
        assertEquals(3L, claims.get("tenantId", Long.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void shouldRejectInvalidToken() {

        assertThrows(
                JwtException.class,
                () -> jwtService.extractClaims("invalid.jwt.token")
        );
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {

        JwtService anotherJwtService = new JwtService();

        ReflectionTestUtils.setField(
                anotherJwtService,
                "secret",
                "QW5vdGhlclRlc3RTZWNyZXRLZXlGb3JIZWFkZXIxMjM0NTY="
        );

        ReflectionTestUtils.setField(
                anotherJwtService,
                "expiration",
                86400000L
        );

        String token = jwtService.generateToken(
                "user@techpulse.com",
                1L,
                "USER"
        );

        assertThrows(
                JwtException.class,
                () -> anotherJwtService.extractClaims(token)
        );
    }
}
